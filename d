import pandas as pd
from openpyxl import load_workbook
from openpyxl.styles import PatternFill
from openpyxl.utils import get_column_letter

def difference(self, key, df1, df2, output_filename):
    # Allow 'key' to be a single column name (string) or a list of column names
    if isinstance(key, str):
        key = [key]  # Convert to list for consistent handling

    # Duplicates
    df1 = df1.drop_duplicates(subset=key)
    df2 = df2.drop_duplicates(subset=key)

    # Merge the dataframes on keys, keeping only the rows with matching keys
    merged_df = df1.merge(df2, on=key, suffixes=('_left', '_right'), how='inner')

    columns_with_diff = set()
    rows_with_diff = {}

    # Check differences
    for row in merged_df.itertuples(index=False):
        row_has_diff = False
        row_data = {}
        for col in df1.columns:
            if col in key:
                continue  # Skip key columns
            left_val = getattr(row, "{}_left".format(col), None)
            right_val = getattr(row, "{}_right".format(col), None)
            if left_val != right_val and not (pd.isna(left_val) and pd.isna(right_val)):
                columns_with_diff.add(col)
                row_data["{}_left".format(col)] = left_val
                row_data["{}_right".format(col)] = right_val
                row_has_diff = True
        if row_has_diff:
            # Use tuple of key values as dict key if multiple, else single value
            row_key = tuple(getattr(row, k) for k in key) if len(key) > 1 else getattr(row, key[0])
            rows_with_diff[row_key] = row_data
    if not rows_with_diff:
        print("No differences found")
        return
    else:
        diff_df = pd.DataFrame.from_dict(rows_with_diff, orient='index')

    # Writing to Excel with highlighting differences
    with pd.ExcelWriter(output_filename, engine='openpyxl') as writer:
        diff_df.to_excel(writer, index=True)  # Index might be useful for composite keys
        writer.close()

    workbook = load_workbook(output_filename)
    worksheet = workbook.active

    red_fill = PatternFill(start_color='FF0000', end_color='FF0000', fill_type='solid')

    # Applying highlighting
    row_start = 2
    for idx, row in enumerate(diff_df.itertuples(index=True), start=row_start):
        for col_name in diff_df.columns:
            if '_left' in col_name or '_right' in col_name:
                base_col = col_name.replace('_left', '').replace('_right', '')
                col_idx = get_column_letter(diff_df.columns.get_loc("{}_left".format(base_col)) + 1)
                if base_col in columns_with_diff:
                    left_val = getattr(row, "{}_left".format(base_col), None)
                    right_val = getattr(row, "{}_right".format(base_col), None)
                    if left_val != right_val and not (pd.isna(left_val) and pd.isna(right_val)):
                        if '_left' in col_name:
                            worksheet["{}{}".format(col_idx, idx)].value = left_val
                            worksheet["{}{}".format(col_idx, idx)].fill = red_fill
                        else:
                            right_col_idx = get_column_letter(diff_df.columns.get_loc("{}_left".format(base_col)) + 2)
                            worksheet["{}{}".format(right_col_idx, idx)].value = right_val
                            worksheet["{}{}".format(right_col_idx, idx)].fill = red_fill

    workbook.save(output_filename)
    print('Differences exported with red highlighting')
