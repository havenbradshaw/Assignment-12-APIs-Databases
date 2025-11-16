package com.assignment12.api;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CountryTableModel extends AbstractTableModel {
    private final List<Country> data = new ArrayList<>();
    private final String[] cols = {"Name", "Population", "Region"};

    public void setData(List<Country> list) {
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    public List<Country> getData() { return new ArrayList<>(data); }

    public Country get(int row) { return data.get(row); }

    @Override
    public int getRowCount() { return data.size(); }

    @Override
    public int getColumnCount() { return cols.length; }

    @Override
    public String getColumnName(int col) { return cols[col]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Country c = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return c.getName();
            case 1: return c.getPopulation();
            case 2: return c.getRegion();
            default: return "";
        }
    }
}
