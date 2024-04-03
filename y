# Ensure 'key' is in a list format for consistent handling
key_columns = key if isinstance(key, list) else [key]

for row in merged_df.itertuples(index=False):
    row_has_diff = False
    # Initialize 'row_data' with an explicit 'Key' that combines all key column values
    key_value = tuple(getattr(row, k) for k in key_columns) if len(key_columns) > 1 else getattr(row, key_columns[0])
    row_data = {'Key': key_value}  # This could be a tuple for composite keys or a single value

    for col in df1.columns:
        # Skip if this column is part of the composite key
        if col in key_columns:
            continue
        left_val = getattr(row, "{}_left".format(col), None)
        right_val = getattr(row, "{}_right".format(col), None)
        if left_val != right_val and not (pd.isna(left_val) and pd.isna(right_val)):
            columns_with_diff.add(col)
            row_data["{}_left".format(col)] = left_val
            row_data["{}_right".format(col)] = right_val
            row_has_diff = True
    if row_has_diff:
        # Use 'key_value' directly as the dictionary key
        rows_with_diff[key_value] = row_data
