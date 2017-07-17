/**
 * CFS Command & Data Dictionary search database tables and scripts dialog.
 * Copyright 2017 United States Government as represented by the Administrator
 * of the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.AUTO_COMPLETE_TEXT_SEPARATOR;
import static CCDD.CcddConstants.CANCEL_BUTTON;
import static CCDD.CcddConstants.CLOSE_ICON;
import static CCDD.CcddConstants.LABEL_FONT_BOLD;
import static CCDD.CcddConstants.LABEL_FONT_PLAIN;
import static CCDD.CcddConstants.LABEL_HORIZONTAL_SPACING;
import static CCDD.CcddConstants.LABEL_TEXT_COLOR;
import static CCDD.CcddConstants.LABEL_VERTICAL_SPACING;
import static CCDD.CcddConstants.NUM_HIDDEN_COLUMNS;
import static CCDD.CcddConstants.NUM_REMEMBERED_SEARCHES;
import static CCDD.CcddConstants.OK_BUTTON;
import static CCDD.CcddConstants.PRINT_ICON;
import static CCDD.CcddConstants.SEARCH_ICON;
import static CCDD.CcddConstants.SEARCH_STRINGS;
import static CCDD.CcddConstants.TABLE_BACK_COLOR;
import static CCDD.CcddConstants.TEXT_HIGHLIGHT_COLOR;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.JTextComponent;

import CCDD.CcddClasses.ArrayListMultiple;
import CCDD.CcddClasses.AutoCompleteTextField;
import CCDD.CcddConstants.DialogOption;
import CCDD.CcddConstants.SearchDialogType;
import CCDD.CcddConstants.SearchResultsColumnInfo;
import CCDD.CcddConstants.TableSelectionMode;
import CCDD.CcddTableTypeHandler.TypeDefinition;

/******************************************************************************
 * CFS Command & Data Dictionary search database tables, scripts, and event log
 * dialog class
 *****************************************************************************/
@SuppressWarnings("serial")
public class CcddSearchDialog extends CcddDialogHandler
{
    // Class references
    private final CcddMain ccddMain;
    private final CcddTableTypeHandler tableTypeHandler;
    private CcddJTableHandler resultsTable;
    private final CcddEventLogDialog eventLog;

    // Components referenced from multiple methods
    private AutoCompleteTextField searchFld;
    private JCheckBox ignoreCaseCb;
    private JCheckBox allowRegexCb;
    private JCheckBox dataTablesOnlyCb;
    private JCheckBox selectedColumnsCb;
    private JLabel numResultsLbl;

    // String containing the names of columns, separated by commas, to which to
    // constrain a table search
    private String searchColumns;

    // Search dialog type
    private final SearchDialogType searchDlgType;

    // Array to contain the search results
    private Object[][] resultsData;

    /**************************************************************************
     * Search database tables, scripts, and event log dialog class constructor
     * 
     * @param ccddMain
     *            main class
     * 
     * @param searchType
     *            search dialog type: TABLES, SCRIPTS, or LOG
     * 
     * @param targetRow
     *            row index to match if this is an event log entry search on a
     *            table that displays only a single log entry; null otherwise
     * 
     * @param eventLog
     *            event log to search; null if not searching a log
     *************************************************************************/
    CcddSearchDialog(CcddMain ccddMain,
                     SearchDialogType searchType,
                     Long targetRow,
                     CcddEventLogDialog eventLog)
    {
        this.ccddMain = ccddMain;
        this.searchDlgType = searchType;
        this.eventLog = eventLog;

        // Create reference to shorten subsequent calls
        tableTypeHandler = ccddMain.getTableTypeHandler();

        // Initialize the search results table contents
        resultsData = new Object[0][0];

        // Create the database table search dialog
        initialize(targetRow);
    }

    /**************************************************************************
     * Search database tables and scripts class constructor
     * 
     * @param ccddMain
     *            main class
     * 
     * @param searchType
     *            search dialog type: TABLES or SCRIPTS
     *************************************************************************/
    CcddSearchDialog(CcddMain ccddMain, SearchDialogType searchType)
    {
        this(ccddMain, searchType, null, null);
    }

    /**************************************************************************
     * Create the database table or scripts search dialog
     * 
     * @param targetRow
     *            row index to match if this is an event log entry search on a
     *            table that displays only a single log entry; null otherwise
     *************************************************************************/
    private void initialize(final Long targetRow)
    {
        final CcddSearchHandler searchHandler = new CcddSearchHandler(ccddMain,
                                                                      searchDlgType,
                                                                      targetRow,
                                                                      eventLog);

        searchColumns = "";

        // Create a border for the dialog components
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                                                                                           Color.LIGHT_GRAY,
                                                                                           Color.GRAY),
                                                           BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // Set the initial layout manager characteristics
        GridBagConstraints gbc = new GridBagConstraints(0,
                                                        0,
                                                        1,
                                                        1,
                                                        1.0,
                                                        0.0,
                                                        GridBagConstraints.LINE_START,
                                                        GridBagConstraints.BOTH,
                                                        new Insets(LABEL_VERTICAL_SPACING / 2,
                                                                   LABEL_HORIZONTAL_SPACING,
                                                                   0,
                                                                   LABEL_HORIZONTAL_SPACING),
                                                        0,
                                                        0);

        // Create panels to hold the components of the dialog
        JPanel dialogPnl = new JPanel(new GridBagLayout());
        dialogPnl.setBorder(BorderFactory.createEmptyBorder());

        // Create the search dialog labels and fields
        JLabel dlgLbl = new JLabel("Enter search text");
        dlgLbl.setFont(LABEL_FONT_BOLD);
        dialogPnl.add(dlgLbl, gbc);

        // Create the auto-completion search field, using the list of
        // remembered searches from the program preferences, and add it to the
        // dialog panel
        List<String> searches = new ArrayList<String>(NUM_REMEMBERED_SEARCHES);
        searches.addAll(Arrays.asList(ccddMain.getProgPrefs().get(SEARCH_STRINGS,
                                                                  "").split(AUTO_COMPLETE_TEXT_SEPARATOR)));
        searchFld = new AutoCompleteTextField(searches,
                                              NUM_REMEMBERED_SEARCHES);
        searchFld.setCaseSensitive(true);
        searchFld.setText("");
        searchFld.setColumns(20);
        searchFld.setFont(LABEL_FONT_PLAIN);
        searchFld.setEditable(true);
        searchFld.setForeground(Color.BLACK);
        searchFld.setBackground(Color.WHITE);
        searchFld.setBorder(border);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.left = LABEL_HORIZONTAL_SPACING * 2;
        gbc.insets.bottom = LABEL_VERTICAL_SPACING / 2;
        gbc.gridy++;
        dialogPnl.add(searchFld, gbc);

        // Create a check box for ignoring the text case
        ignoreCaseCb = new JCheckBox("Ignore text case");
        ignoreCaseCb.setFont(LABEL_FONT_BOLD);
        ignoreCaseCb.setBorder(BorderFactory.createEmptyBorder());
        ignoreCaseCb.setToolTipText("Ignore case when matching the search string");

        // Add a listener for check box selection changes
        ignoreCaseCb.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Handle a change in the ignore case check box state
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                // Change the case sensitivity for the remembered searches to
                // match the case sensitivity check box
                searchFld.setCaseSensitive(!ignoreCaseCb.isSelected());
            }
        });

        gbc.insets.left = LABEL_HORIZONTAL_SPACING;
        gbc.gridy++;
        dialogPnl.add(ignoreCaseCb, gbc);

        // Create a check box for allow a regular expression in the search
        // string
        allowRegexCb = new JCheckBox("Allow regular expression");
        allowRegexCb.setFont(LABEL_FONT_BOLD);
        allowRegexCb.setBorder(BorderFactory.createEmptyBorder());
        allowRegexCb.setToolTipText("Allow the search string to contain a regular expression");
        gbc.insets.left = LABEL_HORIZONTAL_SPACING;
        gbc.gridy++;
        dialogPnl.add(allowRegexCb, gbc);

        // Check if this is a table search
        if (searchDlgType == SearchDialogType.TABLES)
        {
            final ArrayListMultiple columns = new ArrayListMultiple();

            // Create a check box for ignoring matches within the internal
            // tables
            dataTablesOnlyCb = new JCheckBox("Search data table cells only");
            dataTablesOnlyCb.setFont(LABEL_FONT_BOLD);
            dataTablesOnlyCb.setBorder(BorderFactory.createEmptyBorder());
            dataTablesOnlyCb.setToolTipText("Search only the cells in the data tables");
            gbc.gridy++;
            dialogPnl.add(dataTablesOnlyCb, gbc);

            // Step through each defined table type
            for (TypeDefinition typeDefn : tableTypeHandler.getTypeDefinitions())
            {
                // Step through each visible column in the table type
                for (int index = NUM_HIDDEN_COLUMNS; index < typeDefn.getColumnCountDatabase(); ++index)
                {
                    // Check if the column name isn't already in the list
                    if (!columns.contains(typeDefn.getColumnNamesUser()[index]))
                    {
                        // Add the visible column name and its corresponding
                        // database name to the list
                        columns.add(new String[] {typeDefn.getColumnNamesUser()[index],
                                                  typeDefn.getColumnNamesDatabase()[index]});
                    }
                }
            }

            // Check if any columns are defined
            if (columns.size() != 0)
            {
                List<String[]> columnNames = new ArrayList<String[]>();

                // Sort the column names alphabetically
                Collections.sort(columns, new Comparator<String[]>()
                {
                    /**********************************************************
                     * Sort the column names, ignoring case
                     *********************************************************/
                    @Override
                    public int compare(String[] col1, String[] col2)
                    {
                        return col1[0].toLowerCase().compareTo(col2[0].toLowerCase());
                    }
                });

                // Create the column selection and add it to the dialog
                selectedColumnsCb = new JCheckBox("Search selected columns");
                selectedColumnsCb.setFont(LABEL_FONT_BOLD);
                selectedColumnsCb.setBorder(BorderFactory.createEmptyBorder());
                selectedColumnsCb.setToolTipText("Search only selected columns in the data tables");
                gbc.gridy++;
                dialogPnl.add(selectedColumnsCb, gbc);

                // Create a panel for the column selection pop-up dialog
                final JPanel columnPnl = new JPanel(new GridBagLayout());
                columnPnl.setBorder(BorderFactory.createEmptyBorder());

                // Step through each column
                for (String[] column : columns)
                {
                    // Add the visible name to the list used to create the
                    // check box panel
                    columnNames.add(new String[] {column[0], null});
                }

                // Create the column name pop-up dialog
                final CcddDialogHandler columnDlg = new CcddDialogHandler();

                // Add the column name check boxes to the dialog
                columnDlg.addCheckBoxes(null,
                                        columnNames.toArray(new String[0][0]),
                                        null,
                                        "",
                                        columnPnl);

                // Add a listener for check box selection changes
                selectedColumnsCb.addActionListener(new ActionListener()
                {
                    /**********************************************************
                     * Handle a change in the selected columns check box state
                     *********************************************************/
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        // Check if the column selection check box is selected
                        if (selectedColumnsCb.isSelected())
                        {
                            // Display a pop-up for choosing which table
                            // columns to search
                            if (columnDlg.showOptionsDialog(CcddSearchDialog.this,
                                                            columnPnl,
                                                            "Select Column(s)",
                                                            DialogOption.OK_CANCEL_OPTION,
                                                            true) == OK_BUTTON)
                            {
                                searchColumns = "";

                                // Step through each column name check box
                                for (int index = 0; index < columnDlg.getCheckBoxes().length; index++)
                                {
                                    // Check if the check box is selected
                                    if (columnDlg.getCheckBoxes()[index].isSelected())
                                    {
                                        // Add the name of the column to the
                                        // constraint string
                                        searchColumns += columns.get(index)[1] + ",";
                                    }
                                }

                                searchColumns = CcddUtilities.removeTrailer(searchColumns, ",");
                            }
                        }
                        // The column selection check box is not selected
                        else
                        {
                            // Blank the column constraint string
                            searchColumns = "";
                        }
                    }
                });
            }
        }

        // Create the results and number of results found labels
        JLabel resultsLbl = new JLabel("Search results");
        resultsLbl.setFont(LABEL_FONT_BOLD);
        resultsLbl.setForeground(LABEL_TEXT_COLOR);
        numResultsLbl = new JLabel();
        numResultsLbl.setFont(LABEL_FONT_PLAIN);
        gbc.insets.top = LABEL_VERTICAL_SPACING;
        gbc.insets.left = LABEL_HORIZONTAL_SPACING;
        gbc.insets.bottom = 0;
        gbc.gridy++;

        // Add the results labels to the dialog
        JPanel resultsPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resultsPnl.add(resultsLbl);
        resultsPnl.add(numResultsLbl);
        dialogPnl.add(resultsPnl, gbc);

        // Create the table to display the search results
        resultsTable = new CcddJTableHandler()
        {
            /******************************************************************
             * Allow multiple line display in the specified columns, depending
             * on search type
             *****************************************************************/
            @Override
            protected boolean isColumnMultiLine(int column)
            {
                return searchDlgType == SearchDialogType.TABLES
                       || (searchDlgType == SearchDialogType.LOG
                       && column == SearchResultsColumnInfo.CONTEXT.ordinal())
                       || (searchDlgType == SearchDialogType.SCRIPTS
                       && (column == SearchResultsColumnInfo.TARGET.ordinal()
                       || column == SearchResultsColumnInfo.CONTEXT.ordinal()));
            }

            /******************************************************************
             * Allow the specified column's cells to be displayed with the text
             * highlighted
             *****************************************************************/
            @Override
            protected boolean isColumnHighlight(int column)
            {
                return column == SearchResultsColumnInfo.CONTEXT.ordinal();
            }

            /******************************************************************
             * Load the search results data into the table and format the table
             * cells
             *****************************************************************/
            @Override
            protected void loadAndFormatData()
            {
                // Place the data into the table model along with the column
                // names, set up the editors and renderers for the table cells,
                // set up the table grid lines, and calculate the minimum width
                // required to display the table information
                setUpdatableCharacteristics(resultsData,
                                            SearchResultsColumnInfo.getColumnNames(searchDlgType),
                                            null,
                                            null,
                                            null,
                                            SearchResultsColumnInfo.getToolTips(searchDlgType),
                                            true,
                                            true,
                                            true,
                                            true);
            }

            /******************************************************************
             * Override the table layout so that extra width is apportioned
             * unequally between the columns when the table is resized
             *****************************************************************/
            @Override
            public void doLayout()
            {
                // Get a reference to the column being resized
                if (getTableHeader() != null
                    && getTableHeader().getResizingColumn() == null)
                {
                    // Get a reference to the event table's column model to
                    // shorten subsequent calls
                    TableColumnModel tcm = getColumnModel();

                    // Calculate the change in the search dialog's width
                    int delta = getParent().getWidth() - tcm.getTotalColumnWidth();

                    // Get the reference to the search results table columns
                    TableColumn tgtColumn = tcm.getColumn(SearchResultsColumnInfo.TARGET.ordinal());
                    TableColumn locColumn = tcm.getColumn(SearchResultsColumnInfo.LOCATION.ordinal());
                    TableColumn cntxtColumn = tcm.getColumn(SearchResultsColumnInfo.CONTEXT.ordinal());

                    // Set the columns' widths to its current width plus a
                    // percentage of the the extra width added to the dialog
                    // due to the resize
                    tgtColumn.setPreferredWidth(tgtColumn.getPreferredWidth()
                                                + (int) (delta * 0.25));
                    tgtColumn.setWidth(tgtColumn.getPreferredWidth());
                    locColumn.setPreferredWidth(locColumn.getPreferredWidth()
                                                + (int) (delta * 0.25));
                    locColumn.setWidth(locColumn.getPreferredWidth());
                    cntxtColumn.setPreferredWidth(cntxtColumn.getPreferredWidth()
                                                  + delta - (int) (delta * 0.25) * 2);
                    cntxtColumn.setWidth(cntxtColumn.getPreferredWidth());
                }
                // Table header or resize column not available
                else
                {
                    super.doLayout();
                }
            }

            /******************************************************************
             * Highlight the matching search text in the context column cells
             * 
             * @param component
             *            reference to the table cell renderer component
             * 
             * @param value
             *            cell value
             * 
             * @param isSelected
             *            true if the cell is to be rendered with the selection
             *            highlighted
             * 
             * @param int row cell row, view coordinates
             * 
             * @param column
             *            cell column, view coordinates
             *****************************************************************/
            @Override
            protected void doSpecialRendering(Component component,
                                              String text,
                                              boolean isSelected,
                                              int row,
                                              int column)
            {
                // Check if highlighting is enabled and if the column allows
                // text highlighting
                if (isColumnHighlight(column))
                {
                    Pattern pattern;

                    // Create a highlighter painter
                    DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(isSelected
                                                                                                               ? Color.BLACK
                                                                                                               : TEXT_HIGHLIGHT_COLOR);

                    // Check if case is to be ignored
                    if (ignoreCaseCb.isSelected())
                    {
                        // Create the match pattern with case ignored
                        pattern = Pattern.compile(allowRegexCb.isSelected()
                                                                           ? searchFld.getText()
                                                                           : Pattern.quote(searchFld.getText()),
                                                  Pattern.CASE_INSENSITIVE);
                    }
                    // Only highlight matches with the same case
                    else
                    {
                        // Create the match pattern, preserving case
                        pattern = Pattern.compile(allowRegexCb.isSelected()
                                                                           ? searchFld.getText()
                                                                           : Pattern.quote(searchFld.getText()));
                    }

                    // Create the pattern matcher from the pattern
                    Matcher matcher = pattern.matcher(text);

                    // Find each match in the text string
                    while (matcher.find())
                    {
                        try
                        {
                            // Highlight the matching text. Adjust the
                            // highlight color to account for the cell
                            // selection highlighting so that the search text
                            // is easily readable
                            ((JTextComponent) component).getHighlighter().addHighlight(matcher.start(),
                                                                                       matcher.end(),
                                                                                       painter);
                        }
                        catch (BadLocationException ble)
                        {
                            // Ignore highlighting failure
                        }
                    }
                }
            }
        };

        // Place the table into a scroll pane
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        // Set up the search results table parameters
        resultsTable.setFixedCharacteristics(scrollPane,
                                             false,
                                             ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                                             TableSelectionMode.SELECT_BY_CELL,
                                             true,
                                             TABLE_BACK_COLOR,
                                             false,
                                             true,
                                             LABEL_FONT_PLAIN,
                                             true);

        // Define the panel to contain the table
        JPanel resultsTblPnl = new JPanel();
        resultsTblPnl.setLayout(new BoxLayout(resultsTblPnl, BoxLayout.X_AXIS));
        resultsTblPnl.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        resultsTblPnl.add(scrollPane);

        // Add the table to the dialog
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy++;
        dialogPnl.add(resultsTblPnl, gbc);

        // Search database tables button
        JButton btnSearch = CcddButtonPanelHandler.createButton("Search",
                                                                SEARCH_ICON,
                                                                KeyEvent.VK_O,
                                                                "Search the project database");

        // Add a listener for the Search button
        btnSearch.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Search the database tables and display the results
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                // Check if the search field is blank
                if (searchFld.getText().isEmpty())
                {
                    // Inform the user that the input value is invalid
                    new CcddDialogHandler().showMessageDialog(CcddSearchDialog.this,
                                                              "<html><b>Search text cannot be blank",
                                                              "Invalid Input",
                                                              JOptionPane.WARNING_MESSAGE,
                                                              DialogOption.OK_OPTION);
                }
                // The search field contains text
                else
                {
                    List<Object[]> resultsDataList = null;

                    // Update the search string list
                    searchFld.updateList(searchFld.getText());

                    switch (searchDlgType)
                    {
                        case TABLES:
                        case SCRIPTS:
                            // Search the database tables or scripts and
                            // display the results
                            resultsDataList = searchHandler.searchTablesOrScripts(searchFld.getText(),
                                                                                  ignoreCaseCb.isSelected(),
                                                                                  allowRegexCb.isSelected(),
                                                                                  (searchDlgType == SearchDialogType.TABLES
                                                                                                                           ? dataTablesOnlyCb.isSelected()
                                                                                                                           : false),
                                                                                  searchColumns);
                            break;

                        case LOG:
                            // Search the event log and display the results
                            resultsDataList = searchHandler.searchEventLogFile(searchFld.getText(),
                                                                               ignoreCaseCb.isSelected(),
                                                                               targetRow);
                            break;
                    }

                    // Convert the results list to an array and display the
                    // results in the dialog's search results table
                    resultsData = resultsDataList.toArray(new Object[0][0]);
                    resultsTable.loadAndFormatData();
                }

                // Update the number of results found label
                numResultsLbl.setText("  (" + resultsData.length + " found)");
            }
        });

        // Print inconsistencies button
        JButton btnPrint = CcddButtonPanelHandler.createButton("Print",
                                                               PRINT_ICON,
                                                               KeyEvent.VK_P,
                                                               "Print the search results list");

        // Add a listener for the Print button
        btnPrint.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Print the search results list
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                resultsTable.printTable("Search Results",
                                        null,
                                        CcddSearchDialog.this,
                                        PageFormat.LANDSCAPE);
            }
        });

        // Close search dialog button
        JButton btnClose = CcddButtonPanelHandler.createButton("Close",
                                                               CLOSE_ICON,
                                                               KeyEvent.VK_C,
                                                               "Close the search dialog");

        // Add a listener for the Close button
        btnClose.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Store the search strings and close the search dialog
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                // Store the search list in the program preferences
                ccddMain.getProgPrefs().put(SEARCH_STRINGS, searchFld.getListAsString());

                // Close the dialog
                closeDialog(CANCEL_BUTTON);
            }
        });

        // Create a panel for the dialog buttons and add the buttons to the
        // panel
        JPanel buttonPnl = new JPanel();
        buttonPnl.setBorder(BorderFactory.createEmptyBorder());
        buttonPnl.add(btnSearch);
        buttonPnl.add(btnPrint);
        buttonPnl.add(btnClose);

        // Get the dialog title based on the search type
        String title = null;

        switch (searchDlgType)
        {
            case TABLES:
                title = "Search Tables";
                break;

            case SCRIPTS:
                title = "Search Scripts";
                break;

            case LOG:
                title = "Search Event Log";
                break;
        }

        // Display the search dialog
        showOptionsDialog(ccddMain.getMainFrame(),
                          dialogPnl,
                          buttonPnl,
                          title,
                          true);
    }
}
