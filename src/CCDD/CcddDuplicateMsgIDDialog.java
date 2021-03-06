/**
 * CFS Command & Data Dictionary duplicate message ID dialog.
 *
 * Copyright 2017 United States Government as represented by the Administrator
 * of the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved.
 */
package CCDD;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import CCDD.CcddClasses.ArrayListMultiple;
import CCDD.CcddConstants.DialogOption;
import CCDD.CcddConstants.DuplicateMsgIDColumnInfo;
import CCDD.CcddConstants.ModifiableColorInfo;
import CCDD.CcddConstants.ModifiableFontInfo;
import CCDD.CcddConstants.ModifiableSpacingInfo;
import CCDD.CcddConstants.TableSelectionMode;

/******************************************************************************
 * CFS Command & Data Dictionary show duplicate message IDs dialog class
 *****************************************************************************/
@SuppressWarnings("serial")
public class CcddDuplicateMsgIDDialog extends CcddDialogHandler
{
    // List of message IDs that are used by multiple owners, and their owner
    private ArrayListMultiple duplicates;

    /**************************************************************************
     * Show duplicate message IDs dialog class constructor
     *
     * @param ccddMain
     *            main class
     *************************************************************************/
    CcddDuplicateMsgIDDialog(CcddMain ccddMain)
    {
        // Create a message ID handler
        CcddMessageIDHandler msgIDHandler = new CcddMessageIDHandler(ccddMain, true);

        // Create the message ID dialog
        initialize(msgIDHandler, ccddMain.getMainFrame());
    }

    /**************************************************************************
     * Display the duplicate message ID dialog
     *
     * @param msgIDHandler
     *            message ID handler reference
     *
     * @param parent
     *            GUI component calling this method
     *************************************************************************/
    private void initialize(CcddMessageIDHandler msgIDHandler, Component parent)
    {
        // Get the list of message IDs in use - this creates the duplicates
        // list
        msgIDHandler.getMessageIDsInUse(true, true, true, true, true, false, null, true, parent);

        // Get the list of duplicate message IDs
        duplicates = msgIDHandler.getDuplicates();

        // Sort the duplicates list
        Collections.sort(duplicates, new Comparator<String[]>()
        {
            /******************************************************************
             * Sort the duplicates list based on the message ID (second column)
             *****************************************************************/
            @Override
            public int compare(final String[] msgID1, final String[] msgID2)
            {
                return Integer.decode(msgID1[1]).compareTo(Integer.decode(msgID2[1]));
            }
        });

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

        // Create the table to display the duplicate message IDs
        CcddJTableHandler duplicatesTable = new CcddJTableHandler()
        {
            /******************************************************************
             * Allow multiple line display in the specified columns
             *****************************************************************/
            @Override
            protected boolean isColumnMultiLine(int column)
            {
                return column == DuplicateMsgIDColumnInfo.OWNERS.ordinal();
            }

            /******************************************************************
             * Load the duplicate message ID data into the table and format the
             * table cells
             *****************************************************************/
            @Override
            protected void loadAndFormatData()
            {
                // Place the data into the table model along with the column
                // names, set up the editors and renderers for the table cells,
                // set up the table grid lines, and calculate the minimum width
                // required to display the table information
                setUpdatableCharacteristics(duplicates.toArray(new String[0][0]),
                                            DuplicateMsgIDColumnInfo.getColumnNames(),
                                            "1:0",
                                            null,
                                            null,
                                            DuplicateMsgIDColumnInfo.getToolTips(),
                                            true,
                                            true,
                                            true,
                                            true);
            }
        };

        // Place the table into a scroll pane
        JScrollPane scrollPane = new JScrollPane(duplicatesTable);

        // Set up the field table parameters
        duplicatesTable.setFixedCharacteristics(scrollPane,
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

        showOptionsDialog(parent,
                          dialogPnl,
                          "Duplicate Message IDs",
                          DialogOption.PRINT_OPTION,
                          true);
    }
}
