from utils_airtable import fetch_records
import json

records = fetch_records()

staging_data = []
for rec in records:
    fields = rec["fields"]
    fields["recordId"] = rec["id"]
    fields.setdefault("version", 1)
    fields["needsReview"] = False
    staging_data.append(fields)

with open("../data/rituals_staging.json", "w") as f:
    json.dump(staging_data, f, indent=2)

print(f"✅ Synced {len(staging_data)} incomplete rituals to rituals_staging.json")
