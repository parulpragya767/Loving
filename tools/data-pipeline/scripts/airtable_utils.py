import os
from typing import List, Dict, Any, Optional
from pyairtable import Api
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Airtable configuration - moved from individual scripts
AIRTABLE_TOKEN: str = os.getenv('AIRTABLE_TOKEN')
AIRTABLE_BASE_ID: str = "appt8THxSUPVnWkJk"
AIRTABLE_TABLE_NAME: str = "Rituals"
AIRTABLE_VIEW_NAME: Optional[str] = "View1"
AIRTABLE_PAGE_SIZE: int = 100 # Airtable limits to 100

def get_airtable_table():
    """
    Creates and returns an Airtable table instance.
    
    Returns:
        pyairtable.Table instance
    """
    if not AIRTABLE_TOKEN:
        raise ValueError("Airtable API key is required. Set AIRTABLE_TOKEN environment variable.")
    
    # Initialize API and table
    api = Api(AIRTABLE_TOKEN)
    table = api.table(AIRTABLE_BASE_ID, AIRTABLE_TABLE_NAME)
    return table
    
def read_from_airtable(
    filter: Optional[str] = None,
    start: int = 0,
    end: Optional[int] = None,
    max_records: Optional[int] = None,
) -> List[Dict[str, Any]]:
    """
    Returns:
        List of Airtable records with 'id' and 'fields' keys
    """
    # Get table instance
    table = get_airtable_table()
    
    # Build query parameters
    kwargs = {}
    if AIRTABLE_VIEW_NAME:
        kwargs['view'] = AIRTABLE_VIEW_NAME
    if filter:
        kwargs['formula'] = filter
    if max_records:
        kwargs['max_records'] = max_records
    kwargs['page_size'] = AIRTABLE_PAGE_SIZE
    
    # Use pyairtable's built-in pagination
    all_records = table.all(**kwargs)
    
    # Apply start/end slicing if specified
    if end is not None:
        all_records = all_records[start:end]
    elif start > 0:
        all_records = all_records[start:]
    
    return all_records

def create_airtable_records(records: List[Dict[str, Any]]) -> bool:
    """
    Batch create records in Airtable using pyairtable.
    
    Args:
        records: List of records to create. Each record should be a dict with 'fields' key.
                
    Returns:
        True if successful, False otherwise
    """
    if not records:
        return True
        
    try:
        # Get table instance
        table = get_airtable_table()
        
        # Use pyairtable's batch_create method
        table.batch_create([record['fields'] for record in records])
        return True
        
    except Exception as e:
        print(f"Error during batch creation to Airtable: {e}")
        return False

def update_airtable(records: List[Dict[str, Any]]) -> bool:
    """
    Batch update records in Airtable using pyairtable.
    
    Args:
        records: List of records to update. Each record should be a dict with:
                - 'id': The Airtable record ID
                - 'fields': Dict of field names to values to update
                
    Returns:
        True if successful, False otherwise
    """
    if not records:
        return True
        
    try:
        # Get table instance
        table = get_airtable_table()
        
        # Use pyairtable's batch_update method
        table.batch_update(records)
        return True
    except Exception as e:
        print(f"Error during batch update to Airtable: {e}")
        return False
