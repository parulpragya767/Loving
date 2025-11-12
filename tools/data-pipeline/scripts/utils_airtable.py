import requests, os

# ---- CONFIG ----
AIRTABLE_TOKEN = "patvAXvsXoLKD6CVc.47809a79d4aaab40d8829259cf47d9083d2e81359bc626e3ed71f7128d011c06"
BASE_ID = "appt8THxSUPVnWkJk"
TABLE_NAME = "Rituals"
VIEW_NAME = "View1"

BASE_URL = f"https://api.airtable.com/v0/{BASE_ID}/{TABLE_NAME}?view={VIEW_NAME}"
HEADERS = {"Authorization": f"Bearer {AIRTABLE_TOKEN}", "Content-Type": "application/json"}

def fetch_records(filter_formula=None):
    url = BASE_URL
    params = {}
    if filter_formula:
        params["filterByFormula"] = filter_formula
    resp = requests.get(url, headers=HEADERS, params=params)
    resp.raise_for_status()
    return resp.json()["records"]

def update_record(record_id, fields):
    url = f"{BASE_URL}/{record_id}"
    payload = {"fields": fields}
    resp = requests.patch(url, headers=HEADERS, json=payload)
    resp.raise_for_status()
    return resp.json()
