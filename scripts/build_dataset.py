"""
One-time data-prep script: cleans cars_ds_final.csv into a typed JSON dataset
for the car-buying suggestion feature. Not part of the running app -
rerun manually if the source CSV changes.

Usage: python3 scripts/build_dataset.py
"""

import csv
import json
import os
import re

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE_CSV = os.path.join(REPO_ROOT, "cars_ds_final.csv")
OUTPUT_JSON = os.path.join(
    REPO_ROOT, "backend", "src", "main", "resources", "data", "cars.json"
)

NUMBER_RE = re.compile(r"[\d,]+\.?\d*")
POWER_RE = re.compile(r"([\d.]+)\s*(?:PS|Bhp|Hp)\s*@?\s*([\d,]+)?", re.IGNORECASE)
TORQUE_RE = re.compile(r"([\d.]+)\s*Nm\s*@?\s*([\d,]+)?", re.IGNORECASE)


def clean(value):
    if value is None:
        return None
    value = value.strip()
    return value if value else None


def parse_number(value):
    value = clean(value)
    if not value:
        return None
    match = NUMBER_RE.search(value)
    if not match:
        return None
    number = match.group().replace(",", "")
    return float(number) if "." in number else int(number)


def parse_price(value):
    value = clean(value)
    if not value:
        return None
    digits = re.sub(r"[^\d]", "", value)
    return int(digits) if digits else None


def parse_power(value):
    value = clean(value)
    if not value:
        return None, None
    match = POWER_RE.search(value)
    if not match:
        return None, None
    bhp = float(match.group(1))
    rpm = int(match.group(2).replace(",", "")) if match.group(2) else None
    return bhp, rpm


def parse_torque(value):
    value = clean(value)
    if not value:
        return None, None
    match = TORQUE_RE.search(value)
    if not match:
        return None, None
    nm = float(match.group(1))
    rpm = int(match.group(2).replace(",", "")) if match.group(2) else None
    return nm, rpm


def parse_bool(value):
    value = clean(value)
    return value is not None and value.lower() != "no"


def build_row(row):
    power_bhp, power_rpm = parse_power(row.get("Power"))
    torque_nm, torque_rpm = parse_torque(row.get("Torque"))

    return {
        "make": clean(row.get("Make")),
        "model": clean(row.get("Model")),
        "variant": clean(row.get("Variant")),
        "price": parse_price(row.get("Ex-Showroom_Price")),
        "priceDisplay": clean(row.get("Ex-Showroom_Price")),
        "bodyType": clean(row.get("Body_Type")),
        "fuelType": clean(row.get("Fuel_Type")),
        "transmission": clean(row.get("Type")),
        "seatingCapacity": parse_number(row.get("Seating_Capacity")),
        "doors": parse_number(row.get("Doors")),
        "drivetrain": clean(row.get("Drivetrain")),
        "displacementCc": parse_number(row.get("Displacement")),
        "cylinders": parse_number(row.get("Cylinders")),
        "gears": parse_number(row.get("Gears")),
        "powerBhp": power_bhp,
        "powerRpm": power_rpm,
        "torqueNm": torque_nm,
        "torqueRpm": torque_rpm,
        "cityMileage": parse_number(row.get("City_Mileage")),
        "highwayMileage": parse_number(row.get("Highway_Mileage")),
        "araiMileage": parse_number(row.get("ARAI_Certified_Mileage")),
        "araiMileageCng": parse_number(row.get("ARAI_Certified_Mileage_for_CNG")),
        "fuelTankCapacity": parse_number(row.get("Fuel_Tank_Capacity")),
        "length": parse_number(row.get("Length")),
        "width": parse_number(row.get("Width")),
        "height": parse_number(row.get("Height")),
        "wheelbase": parse_number(row.get("Wheelbase")),
        "groundClearance": parse_number(row.get("Ground_Clearance")),
        "kerbWeight": parse_number(row.get("Kerb_Weight")),
        "bootSpace": parse_number(row.get("Boot_Space")),
        "airbagsCount": parse_number(row.get("Number_of_Airbags")),
        "abs": parse_bool(row.get("ABS_(Anti-lock_Braking_System)")),
        "ebd": parse_bool(row.get("EBD_(Electronic_Brake-force_Distribution)")),
        "esp": parse_bool(row.get("ESP_(Electronic_Stability_Program)")),
        "isofix": parse_bool(row.get("ISOFIX_(Child-Seat_Mount)")),
        "powerSteering": parse_bool(row.get("Power_Steering")),
        "powerWindows": parse_bool(row.get("Power_Windows")),
        "bluetooth": parse_bool(row.get("Bluetooth")),
        "androidAuto": parse_bool(row.get("Android_Auto")),
        "appleCarPlay": parse_bool(row.get("Apple_CarPlay")),
        "navigationSystem": parse_bool(row.get("Navigation_System")),
        "cruiseControl": parse_bool(row.get("Cruise_Control")),
        "emissionNorm": clean(row.get("Emission_Norm")),
    }


def main():
    with open(SOURCE_CSV, encoding="latin-1", newline="") as f:
        reader = csv.DictReader(f)
        rows = [build_row(row) for row in reader]

    os.makedirs(os.path.dirname(OUTPUT_JSON), exist_ok=True)
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(rows, f, indent=2, ensure_ascii=False)

    missing_price = sum(1 for r in rows if r["price"] is None)
    missing_power = sum(1 for r in rows if r["powerBhp"] is None)
    print(f"Wrote {len(rows)} cars to {OUTPUT_JSON}")
    print(f"Rows missing price: {missing_price}, missing power: {missing_power}")


if __name__ == "__main__":
    main()