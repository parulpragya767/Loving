Run the scripts like:
python3 -m scripts.sync_rituals --sync_direction to_json
python3 -m scripts.sync_rituals --sync_direction to_airtable
python3 -m scripts.populate_rituals --start 1 --end 10 --prompt-version 6

To generate the schema file run:
python3 -m model.ritual_models