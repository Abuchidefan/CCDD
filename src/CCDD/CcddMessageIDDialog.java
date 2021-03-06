/**
 * CFS Command & Data Dictionary show all message IDs dialog.
 *
 * Copyright 2017 United States Government as represented by the Administrator
 * of the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.CLOSE_ICON;
import static CCDD.CcddConstants.PRINT_ICON;
import static CCDD.CcddConstants.TABLE_ICON;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import CCDD.CcddConstants.MessageIDSortOrder;
import CCDD.CcddConstants.ModifiableColorInfo;
import CCDD.CcddConstants.ModifiableFontInfo;
import CCDD.CcddConstants.ModifiableSpacingInfo;
import CCDD.CcddConstants.MsgIDListColumnIndex;
import CCDD.CcddConstants.MsgIDTableColumnInfo;
import CCDD.CcddConstants.TableSelectionMode;

/******************************************************************************
 * CFS Command & Data Dictionary show all message IDs dialog class
 *****************************************************************************/
@SuppressWarnings("serial")
public class CcddMessageIDDialog extends CcddDialogHandler
{
    // Class reference
    private final CcddDbTableCommandHandler dbTable;

    // Flag that indicates if any of the tables with message IDS to display are
    // children of another table, and therefore have a structure path
    private boolean isPath;

    /**************************************************************************
     * Show all message IDs dialog class constructor
     *
     * @param ccddMain
     *            main class
     *************************************************************************/
    CcddMessageIDDialog(CcddMain ccddMain)
    {
        dbTable = ccddMain.getDbTableCommandHandler();

        // Create a message ID handler
        CcddMessageIDHandler msgIDHandler = new CcddMessageIDHandler(ccddMain, false);

        // Create the message ID dialog
        initialize(msgIDHandler, ccddMain.getMainFrame());
    }

    /**************************************************************************
     * Display the owner, message ID name, and message ID dialog
     *
     * @param msgIDHandler
     *            message ID handler reference
     *
     * @param parent
     *            GUI component calling this method
     *************************************************************************/
    private void initialize(CcddMessageIDHandler msgIDHandler, Component parent)
    {
        final List<String[]> msgIDs = msgIDHandler.getMessageIDsAndNames(MessageIDSortOrder.BY_OWNER,
                                                                         parent);

        // Set the initial layout manager characteristics
        GridBagConstraints gbc = new GridBagConstraints(0,
                                                        0,
                                                        1,
                                                        1,
                                                        1.0,
                                                        0.0,
                                                        GridBagConstraints.LINE_START,
                                                        GridBagConstraints.BOTH,
                                                        new Insets(ModifiableSpacingInfo.LABEL_VERTICAL_SPACING.getSpacing() / 2,
                                                                   ModifiableSpacingInfo.LABEL_HORIZONTAL_SPACING.getSpacing(),
                                                                   0,
                                                                   ModifiableSpacingInfo.LABEL_HORIZONTAL_SPACING.getSpacing()),
                                                        0,
                                                        0);

        // Create panels to hold the components of the dialog
        JPanel dialogPnl = new JPanel(new GridBagLayout());
        dialogPnl.setBorder(BorderFactory.createEmptyBorder());

        // Create the table to display the message IDs and names
        final CcddJTableHandler msgIDTable = new CcddJTableHandler()
        {
            /******************************************************************
             * Allow multiple line display in the all columns
             *****************************************************************/
            @Override
            protected boolean isColumnMultiLine(int column)
            {
                return true;
            }

            /******************************************************************
             * Load the message ID data into the table and format the table
             * cells
             *****************************************************************/
            @Override
            protected void loadAndFormatData()
            {
                // Create lists for any columns to be hidden
                List<Integer> hiddenColumns = new ArrayList<Integer>();

                // Build the message ID table information
                Object[][] messageIDData = getMessageIDsToDisplay(msgIDs);

                // Check if none of the tables to display have paths
                if (!isPath)
                {
                    // Hide the structure path column
                    hiddenColumns.add(MsgIDTableColumnInfo.PATH.ordinal());
                }

                // Place the data into the table model along with the column
                // names, set up the editors and renderers for the table cells,
                // set up the table grid lines, and calculate the minimum width
                // required to display the table information
                setUpdatableCharacteristics(messageIDData,
                                            MsgIDTableColumnInfo.getColumnNames(),
                                            null,
                                            hiddenColumns.toArray(new Integer[0]),
                                            null,
                                            MsgIDTableColumnInfo.getToolTips(),
                                            true,
                                            true,
                                            true,
                                            true);
            }
        };

        // Place the table into a scroll pane
        JScrollPane scrollPane = new JScrollPane(msgIDTable);

        // Set up the field table parameters
        msgIDTable.setFixedCharacteristics(scrollPane,
                                           false,
                                           ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                                           TableSelectionMode.SELECT_BY_CELL,
                                           true,
                                           ModifiableColorInfo.TABLE_BACK.getColor(),
                                           false,
                                           true,
                                           ModifiableFontInfo.OTHER_TABLE_CELL.getFont(),
                                           true);

        // Define the panel to contain the table
        JPanel msgIDTblPnl = new JPanel();
        msgIDTblPnl.setLayout(new BoxLayout(msgIDTblPnl, BoxLayout.X_AXIS));
        msgIDTblPnl.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        msgIDTblPnl.add(scrollPane);

        // Add the table to the dialog
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy++;
        dialogPnl.add(msgIDTblPnl, gbc);

        // Open tables button
        JButton btnOpen = CcddButtonPanelHandler.createButton("Open",
                                                              TABLE_ICON,
                                                              KeyEvent.VK_O,
                                                              "Open the table(s) associated with the selected message ID(s)");

        // Add a listener for the Open button
        btnOpen.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Open the table(s) associated with the selected message ID(s)
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                openTables(msgIDTable);
            }
        });

        // Print message ID table button
        JButton btnPrint = CcddButtonPanelHandler.createButton("Print",
                                                               PRINT_ICON,
                                                               KeyEvent.VK_P,
                                                               "Print the message ID table");

        // Add a listener for the Print button
        btnPrint.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Print the message ID data table
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                msgIDTable.printTable("Message ID owners, names, and ID values",
                                      null,
                                      CcddMessageIDDialog.this,
                                      PageFormat.LANDSCAPE);
            }
        });

        // Close button
        JButton btnClose = CcddButtonPanelHandler.createButton("Close",
                                                               CLOSE_ICON,
                                                               KeyEvent.VK_C,
                                                               "Close the message ID dialog");

        // Add a listener for the Close button
        btnClose.addActionListener(new ActionListener()
        {
            /******************************************************************
             * Close the message ID dialog
             *****************************************************************/
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                closeDialog();
            }
        });

        // Create a button panel and add the buttons to it
        JPanel buttonPnl = new JPanel();
        buttonPnl.add(btnOpen);
        buttonPnl.add(btnPrint);
        buttonPnl.add(btnClose);

        // Display the dialog
        showOptionsDialog(parent,
                          dialogPnl,
                          buttonPnl,
                          null,
                          "Show All Message IDs",
                          true);
    }

    /**************************************************************************
     * Open the table(s) associated with the selected message IDs
     *************************************************************************/
    private void openTables(CcddJTableHandler msgIDTable)
    {
        List<String> tablePaths = new ArrayList<String>();

        // Step through each row in the table
        for (int row = 0; row < msgIDTable.getRowCount(); row++)
        {
            // Step through each column in the table
            for (int column = 0; column < msgIDTable.getColumnCount(); column++)
            {
                // Get the owner for this row
                String ownerName = msgIDTable.getModel().getValueAt(row,
                                                                    MsgIDTableColumnInfo.OWNER.ordinal())
                                             .toString().trim();

                // Check if the cell at these coordinates is selected and that
                // the message ID for this row belongs to a table (versus a
                // group or telemetry scheduler)
                if (msgIDTable.isCellSelected(msgIDTable.convertRowIndexToView(row), column)
                    && !ownerName.startsWith(CcddFieldHandler.getFieldGroupName(""))
                    && !ownerName.startsWith("Tlm:"))
                {
                    // Get the structure path for this row
                    String path = msgIDTable.getModel().getValueAt(row,
                                                                   MsgIDTableColumnInfo.PATH.ordinal())
                                            .toString();

                    // Add the table path to the list and stop checking the
                    // columns in this row
                    tablePaths.add(getOwnerWithPath(ownerName, path));
                    break;
                }
            }
        }

        // Check if any table/field is selected
        if (!tablePaths.isEmpty())
        {
            // Load the selected table's data into a table editor
            dbTable.loadTableDataInBackground(tablePaths.toArray(new String[0]),
                                              null);
        }
    }

    /**************************************************************************
     * Build the data field array
     *
     * @param msgIDs
     *            list containing the message ID owners, names, and ID values
     *
     * @return Array containing the data field owner names and corresponding
     *         user-selected data field values
     *************************************************************************/
    private Object[][] getMessageIDsToDisplay(List<String[]> msgIDs)
    {
        isPath = false;
        List<Object[]> ownerMsgIDs = new ArrayList<Object[]>();

        // Step through each message ID
        for (String[] msgID : msgIDs)
        {
            // Get the message ID owner's name
            String ownerName = msgID[MsgIDListColumnIndex.OWNER.ordinal()];

            String pathName = "";

            // Check that the owner isn't a group or telemetry scheduler
            if (!ownerName.startsWith(CcddFieldHandler.getFieldGroupName(""))
                && !ownerName.startsWith("Tlm:"))
            {
                // Get the index of the last comma in the field table path &
                // name
                int commaIndex = ownerName.lastIndexOf(",");

                // Check if a comma was found in the table path & name
                if (commaIndex != -1)
                {
                    // Extract the path name from the table path and name
                    pathName = ownerName.substring(0, commaIndex);

                    // Count the number of commas in the path name, which
                    // indicates the structure nest level
                    int depth = pathName.split(",").length;

                    // Set the indentation
                    String indent = "";

                    // Step through each nest level
                    for (int count = 0; count < depth; count++)
                    {
                        // Add spaces to the indentation. This aids in
                        // identifying the structure members
                        indent += "  ";
                    }

                    // Remove the path and leave only the table name
                    ownerName = indent + ownerName.substring(commaIndex + 1);

                    // Add spaces after any remaining commas in the path
                    pathName = pathName.replaceAll(",", ", ");

                    // Check if this owner has a path (i.e., it's a structure
                    // table)
                    if (!pathName.isEmpty())
                    {
                        // Set the flag to indicate at least one of the owners
                        // has a path
                        isPath = true;
                    }
                }
            }

            // Add the message ID information to the list
            ownerMsgIDs.add(new Object[] {ownerName,
                                          pathName,
                                          msgID[MsgIDListColumnIndex.MESSAGE_ID_NAME.ordinal()],
                                          msgID[MsgIDListColumnIndex.MESSAGE_ID.ordinal()]});
        }

        return ownerMsgIDs.toArray(new Object[0][0]);
    }

    /**************************************************************************
     * Get the owner name with path, if applicable (child tables of a structure
     * table have a path)
     *
     * @param ownerName
     *            table or group owner name
     *
     * @param path
     *            table path; blank if none
     *
     * @return Table or group name with path, if applicable
     *************************************************************************/
    private String getOwnerWithPath(String ownerName, String path)
    {
        // Remove and leading spaces used for indenting child structure names
        ownerName = ownerName.trim();

        // Check if the owner has a path
        if (!path.isEmpty())
        {
            // Prepend the path to the table name
            ownerName = path.replaceAll(" ", "") + "," + ownerName;
        }

        return ownerName;
    }
}
