import requests
import json
from datetime import datetime

# ---- CONFIG ----
AIRTABLE_TOKEN = "patvAXvsXoLKD6CVc.47809a79d4aaab40d8829259cf47d9083d2e81359bc626e3ed71f7128d011c06"
BASE_ID = "appt8THxSUPVnWkJk"
TABLE_NAME = "Rituals"
JSON_PATH = "rituals_master.json"

AIRTABLE_URL = f"https://api.airtable.com/v0/{BASE_ID}/{TABLE_NAME}?view=View1"
HEADERS = {"Authorization": f"Bearer {AIRTABLE_TOKEN}", "Content-Type": "application/json"}


# ---- FUNCTIONS ----

def load_json():
    with open(JSON_PATH, "r", encoding="utf-8") as f:
        return json.load(f)

def save_json(data):
    with open(JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def upload_to_airtable():
    """Push JSON rituals to Airtable (creates or updates records)."""
    data = load_json()
    print(f"Uploading {len(data)} rituals to Airtable...")

    for ritual in data:
        airtable_record = {
            "fields": {
                "id": ritual.get("id"),
                "title": ritual.get("Title"),
                "tagline": ritual.get("Tagline"),
                "description": ritual.get("Description"),
                "steps": "\n".join(ritual.get("Steps", [])),
                "howItHelps": ritual.get("How It Helps"),
                "loveType": ritual.get("Love Types"),
                "ritualMode": ritual.get("Ritual Mode"),
                "timeTaken": ritual.get("Time Taken"),
                "relationalNeed": ritual.get("Relational Needs"),
                "ritualTone": ritual.get("Ritual Tones"),
                "needsReview": ritual.get("needsReview", False),
                "lastUpdated": datetime.utcnow().isoformat(),
            }
        }

        requests.post(AIRTABLE_URL, headers=HEADERS, json=airtable_record)

    print("✅ Upload complete.")


def download_from_airtable():
    """Fetch all rituals from Airtable and write to JSON."""
    print("Fetching data from Airtable...")
    all_records = []
    offset = None

    while True:
        params = {}
        if offset:
            params["offset"] = offset
        response = requests.get(AIRTABLE_URL, headers=HEADERS, params=params)
        result = response.json()
        all_records.extend(result["records"])
        offset = result.get("offset")
        if not offset:
            break

    rituals = []
    for record in all_records:
        f = record["fields"]
        ritual = {
            "id": f.get("id"),
            "title": f.get("Title"),
            "tagline": f.get("Tagline"),
            "description": f.get("Description"),
            "steps": f.get("Steps", "").split("\n") if f.get("Steps") else [],
            "howItHelps": f.get("How It Helps"),
            "loveType": f.get("Love Types"),
            "ritualMode": f.get("Ritual Mode"),
            "timeTaken": f.get("Time Taken"),
            "relationalNeed": f.get("Relational Needs"),
            "ritualTone": f.get("Ritual Tones"),
            "needsReview": f.get("Needs Review", False),
            "lastUpdated": f.get("Last Updated"),
        }
        rituals.append(ritual)

    save_json(rituals)
    print(f"✅ Downloaded {len(rituals)} rituals to {JSON_PATH}.")


# ---- RUN ----
if __name__ == "__main__":
    mode = input("Enter mode (upload/download): ").strip().lower()
    if mode == "upload":
        upload_to_airtable()
    elif mode == "download":
        download_from_airtable()
    else:
        print("❌ Invalid mode. Use 'upload' or 'download'.")
