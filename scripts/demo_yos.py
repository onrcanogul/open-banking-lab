#!/usr/bin/env python3
"""Lab YÖS: drives the account-information flow end to end against the local stack.

1. TPP token from Keycloak (client_credentials, client TPP1)
2. POST hesap-bilgisi-rizasi to hhs-bank            -> rizaNo + hhsYonAdr
3. Opens hhsYonAdr in the browser; the customer logs in and approves at auth-simulator
4. Catches the redirect to yonAdr (http://localhost:8083/callback) -> yetKod
5. POST erisim-belirteci to auth-simulator          -> erisimBelirteci (riza_no)
6. Waits for the consent to become K, then lists accounts and balances with X-Access-Token

Standard library only:  python3 scripts/demo_yos.py [--customer 11111111110]
"""
import argparse
import datetime as dt
import http.server
import json
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
import webbrowser

KEYCLOAK_TOKEN = "http://localhost:8080/realms/ohvps-bank/protocol/openid-connect/token"
HHS_BANK = "http://localhost:8081/ohvps/hbh/s2.0"
GKD = "http://localhost:8082/ohvps/gkd/s2.0"
CALLBACK_PORT = 8083
YON_ADR = f"http://localhost:{CALLBACK_PORT}/callback"

TPP_CODE, TPP_SECRET, ASPSP_CODE = "TPP1", "tpp1-dev-secret", "HHS1"


def call(method, url, body=None, headers=None, form=False):
    headers = dict(headers or {})
    data = None
    if body is not None:
        if form:
            data = urllib.parse.urlencode(body).encode()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
        else:
            data = json.dumps(body).encode()
            headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as res:
            raw = res.read()
            return res.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as e:
        raw = e.read()
        try:
            payload = json.loads(raw)
        except ValueError:
            payload = raw.decode(errors="replace")
        return e.code, payload


def ohvps_headers(token=None, access_token=None):
    h = {"X-Request-ID": str(uuid.uuid4()), "X-ASPSP-Code": ASPSP_CODE, "X-TPP-Code": TPP_CODE}
    if token:
        h["Authorization"] = f"Bearer {token}"
    if access_token:
        h["X-Access-Token"] = access_token
    return h


def expect(status, payload, ok, step):
    if status not in ok:
        raise SystemExit(f"\n✗ {step} failed: HTTP {status}\n{json.dumps(payload, indent=2, ensure_ascii=False)}")


def wait_for_callback():
    result = {}
    done = threading.Event()

    class Handler(http.server.BaseHTTPRequestHandler):
        def do_GET(self):
            parsed = urllib.parse.urlparse(self.path)
            if parsed.path != "/callback":
                self.send_response(404)
                self.end_headers()
                return
            result.update({k: v[0] for k, v in urllib.parse.parse_qs(parsed.query).items()})
            approved = result.get("rizaDrm") == "Y"
            msg = "Onay alındı, terminale dönebilirsiniz." if approved else "Rıza reddedildi."
            page = f"<!doctype html><meta charset=utf-8><title>Demo YÖS</title><h2>Demo YÖS</h2><p>{msg}</p>"
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(page.encode())
            done.set()

        def log_message(self, *args):
            pass

    server = http.server.HTTPServer(("127.0.0.1", CALLBACK_PORT), Handler)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    return server, done, result


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--customer", default="11111111110", help="kmlkVrs (TCKN) of the customer the consent is for")
    parser.add_argument("--timeout", type=int, default=300, help="seconds to wait for the GKD step")
    args = parser.parse_args()

    print("1) TPP token (Keycloak client_credentials)")
    status, tok = call("POST", KEYCLOAK_TOKEN, {"grant_type": "client_credentials", "client_id": TPP_CODE,
                                                "client_secret": TPP_SECRET}, form=True)
    expect(status, tok, {200}, "TPP token")
    tpp_token = tok["access_token"]

    print("2) Hesap bilgisi rızası")
    expires = (dt.datetime.now().astimezone() + dt.timedelta(days=30)).replace(microsecond=0).isoformat()
    consent_req = {
        "katilimciBlg": {"hhsKod": ASPSP_CODE, "yosKod": TPP_CODE},
        "gkd": {"yetYntm": "Y", "yonAdr": YON_ADR},
        "kmlk": {"kmlkTur": "K", "kmlkVrs": args.customer, "ohkTur": "B"},
        "hspBlg": {"iznBlg": {"iznTur": ["01", "03"], "erisimIzniSonTrh": expires}},
    }
    status, consent = call("POST", f"{HHS_BANK}/hesap-bilgisi-rizasi", consent_req, ohvps_headers(tpp_token))
    expect(status, consent, {201}, "Consent creation")
    riza_no = consent["rzBlg"]["rizaNo"]
    hhs_yon_adr = consent["gkd"]["hhsYonAdr"]
    print(f"   rizaNo={riza_no}  rizaDrm={consent['rzBlg']['rizaDrm']}")

    server, done, callback = wait_for_callback()
    print(f"3) GKD: tarayıcıda açılıyor -> {hhs_yon_adr}")
    print(f"   Müşteri: {args.customer}  (şifreler infra/keycloak/ohvps-bank-realm.json içinde)")
    webbrowser.open(hhs_yon_adr)
    if not done.wait(args.timeout):
        server.shutdown()
        raise SystemExit("✗ GKD tamamlanmadı (zaman aşımı)")
    server.shutdown()
    print(f"4) yonAdr'ye dönüldü: rizaDrm={callback.get('rizaDrm')}")
    if callback.get("rizaDrm") != "Y":
        raise SystemExit("   Rıza onaylanmadı; akış burada biter.")

    print("5) Erişim belirteci (auth-simulator)")
    token_req = {"hhsKod": ASPSP_CODE, "yosKod": TPP_CODE, "yetTip": "yet_kod", "yetKod": callback["yetKod"],
                 "rizaNo": riza_no, "rizaTip": "H"}
    status, tokens = call("POST", f"{GKD}/erisim-belirteci", token_req, ohvps_headers(tpp_token))
    expect(status, tokens, {200}, "Token exchange")
    access_token = tokens["erisimBelirteci"]
    print(f"   gecerlilikSuresi={tokens['gecerlilikSuresi']}s  "
          f"yenilemeBelirteciGecerlilikSuresi={tokens['yenilemeBelirteciGecerlilikSuresi']}s")

    print("6) Rıza durumu K olana kadar bekleniyor (Kafka üzerinden)")
    for _ in range(30):
        status, current = call("GET", f"{HHS_BANK}/hesap-bilgisi-rizasi/{riza_no}", headers=ohvps_headers(tpp_token))
        expect(status, current, {200}, "Consent read")
        if current["rzBlg"]["rizaDrm"] == "K":
            break
        time.sleep(0.5)
    else:
        raise SystemExit(f"✗ Rıza K olmadı (son durum {current['rzBlg']['rizaDrm']})")
    print("   rizaDrm=K")

    print("7) Hesaplar ve bakiyeler (X-Access-Token)")
    status, accounts = call("GET", f"{HHS_BANK}/hesaplar", headers=ohvps_headers(access_token=access_token))
    expect(status, accounts, {200}, "Account list")
    for account in accounts:
        ref = account["accountRef"]
        status, balance = call("GET", f"{HHS_BANK}/hesaplar/{ref}/bakiye", headers=ohvps_headers(access_token=access_token))
        expect(status, balance, {200}, f"Balance {ref}")
        print(f"   {ref}  {account['iban']}  {balance['bakiyeTutar']} {balance['prBrm']}")

    print("8) Yenileme belirteci ile yeni erişim belirteci")
    refresh_req = dict(token_req, yetTip="yenileme_belirteci", yetKod=tokens["yenilemeBelirteci"])
    status, refreshed = call("POST", f"{GKD}/erisim-belirteci", refresh_req, ohvps_headers(tpp_token))
    expect(status, refreshed, {200}, "Token refresh")
    status, _ = call("GET", f"{HHS_BANK}/hesaplar", headers=ohvps_headers(access_token=refreshed["erisimBelirteci"]))
    expect(status, _, {200}, "Account list with refreshed token")
    print("   yeni belirteçle hesap listesi: OK")

    print("\n✓ Uçtan uca akış tamamlandı")


if __name__ == "__main__":
    main()
