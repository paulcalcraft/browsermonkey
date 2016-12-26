package browsermonkey.render;

import browsermonkey.document.*;
import browsermonkey.utility.BrowserMonkeyLogger;
import java.text.AttributedCharacterIterator.Attribute;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.ArrayList;

/**
 * Renders tables from table TagDocumentNodes and their children.
 * @author Paul Calcraft
 */
public class TableTagRenderer extends TagRenderer {

    public TableTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Calculate border thickness.

        int borderThickness = 1; // Default border thickness.
        
        String border;
        if ((border = tag.getAttribute("border")) != null) {
            try {
                borderThickness = Integer.parseInt(border);
            } catch (NumberFormatException ex) {
                BrowserMonkeyLogger.conformance("Invalid border attribute value \""+border+"\" in table tag.");
                renderer.foundConformanceError();
                borderThickness = 1;
            }
        }

        // Create new table node.
        TableRenderNode tableNode = new TableRenderNode(linker, borderThickness);

        for (DocumentNode rowNode : tag.getChildren()) {
            // Only render tr nodes as rows.
            if (!(rowNode instanceof TagDocumentNode) || !((TagDocumentNode)rowNode).getType().equals("tr"))
                continue;

            // Start new row.
            tableNode.newRow();

            TagDocumentNode row = (TagDocumentNode)rowNode;

            // Get child td tags as cells.
            for (DocumentNode cellNode : row.getChildren()) {
                // Only render td nodes as cells.
                if (!(cellNode instanceof TagDocumentNode) || !((TagDocumentNode)cellNode).getType().equals("td"))
                    continue;

                TagDocumentNode cell = (TagDocumentNode)cellNode;
                LayoutRenderNode cellRender = new LayoutRenderNode(linker);
                // Set cell padding to standard table cell padding.
                cellRender.setPadding(8, 8, 8, 8);

                // Render children into this cell.
                for (DocumentNode child : cell.getChildren())
                    renderer.render(child, cellRender, formatting);

                // Add the cell to the table (at the current row).
                tableNode.addCell(cellRender);
            }
        }

        // Add the table to the current parent.
        parent.addNode(tableNode, LayoutRenderNode.WidthBehaviour.Maximal);
    }


    /**
     * Swing component (RenderNode) for table rendering.
     */
    private static class TableRenderNode extends LayoutRenderNode {
        private GroupLayout.SequentialGroup horizontalSequence;
        private GroupLayout.SequentialGroup verticalSequence;
        private ArrayList<GroupLayout.ParallelGroup> rowGroups;
        private ArrayList<GroupLayout.ParallelGroup> columnGroups;

        // Multi-dimensional array to hold rows and columns.
        private ArrayList<ArrayList<LayoutRenderNode>> tableCells;

        private int currentRowIndex = -1;
        private int currentColumnIndex = -1;
        private GroupLayout layout;
        private int borderThickness;

        public TableRenderNode(Linkable linker, int borderThickness) {
            super(linker);
            this.borderThickness = borderThickness;
            layout = new GroupLayout(this);
            this.setLayout(layout);
            horizontalSequence = layout.createSequentialGroup();
            verticalSequence = layout.createSequentialGroup();
            layout.setHorizontalGroup(horizontalSequence);
            layout.setVerticalGroup(verticalSequence);
            rowGroups = new ArrayList<GroupLayout.ParallelGroup>();
            columnGroups = new ArrayList<GroupLayout.ParallelGroup>();

            tableCells = new ArrayList<ArrayList<LayoutRenderNode>>();
        }

        // Add vertical gap for a row border.
        private void addRowBorder() {
            verticalSequence.addGap(borderThickness);
        }

        // Add horizontal gap for a column border.
        private void addColumnBorder() {
            horizontalSequence.addGap(borderThickness);
        }

        /**
         * Creates a new row.
         */
        public void newRow() {
            // If first row, add top border.
            if (currentRowIndex == -1)
                addRowBorder();

            // Create parallel group to keep heights of this row's columns in
            // sync.
            GroupLayout.ParallelGroup rowLayout = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
            verticalSequence.addGroup(rowLayout);
            rowGroups.add(rowLayout);
            // Add a new array list of layout nodes, which is the array of
            // columns in this row.
            tableCells.add(new ArrayList<LayoutRenderNode>());

            // Increase row index and set column index to 0, start of new row.
            currentRowIndex++;
            currentColumnIndex = 0;

            // Add the row border below.
            addRowBorder();
        }

        // Creates an empty cell.
        private LayoutRenderNode createEmptyCell() {
            LayoutRenderNode result = new LayoutRenderNode(linker);
            result.getTextNode().addText("&nbsp;", Renderer.DEFAULT_FORMATTING);
            return result;
        }

        /**
         * Adds a column cell to the current row.
         * @param cell the layout node to add as the column
         */
        public void addCell(LayoutRenderNode cell) {
            GroupLayout.ParallelGroup columnLayout;
            // If this is the first column at this index...
            if (currentColumnIndex >= columnGroups.size()) {
                // If first column, add the left border.
                if (currentColumnIndex == 0)
                    addColumnBorder();

                // Create parallel group to keep widths of this column's cells
                // in sync.
                columnLayout = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
                columnGroups.add(columnLayout);
                horizontalSequence.addGroup(columnLayout);

                // For all previous rows (if any), add an empty cell to keep the
                // number of columns per row the same.
                for (int i = 0; i < currentRowIndex; i++) {
                    LayoutRenderNode emptyCell = createEmptyCell();
                    rowGroups.get(i).addComponent(emptyCell);
                    columnLayout.addComponent(emptyCell);
                    tableCells.get(i).add(emptyCell);
                }

                // Add border to right of column.
                addColumnBorder();
            }
            // Else retrieve the column layout group with the column index.
            else
                columnLayout = columnGroups.get(currentColumnIndex);

            // Add the cell to the current column and row layout.
            columnLayout.addComponent(cell);
            rowGroups.get(currentRowIndex).addComponent(cell);

            // Add the cell to the array of cells for the current row.
            tableCells.get(currentRowIndex).add(cell);
            
            currentColumnIndex++;
        }

        @Override
        public void paint(Graphics g) {
            // Call super to draw child nodes.
            super.paint(g);

            // Only draw border if there's one set and the table has cells.
            if (borderThickness == 0 || tableCells == null || tableCells.size() == 0 || tableCells.get(0) == null ||  tableCells.get(0).size() == 0)
                return;

            // Iterate through the table to find the maximum widths and heights
            // of the columns and rows.
            int[] rowHeights = new int[tableCells.size()];
            int[] columnWidths = new int[tableCells.get(0).size()];

            for (int i = 0; i < tableCells.size(); i++) {
                ArrayList<LayoutRenderNode> row = tableCells.get(i);
                for (int j = 0; j < row.size(); j++) {
                    LayoutRenderNode column = row.get(j);
                    rowHeights[i] = Math.max(rowHeights[i], column.getHeight());
                    columnWidths[j] = Math.max(columnWidths[j], column.getWidth());
                }
            }

            // Draw the borders according to these calculated dimensions.

            // First row borders.
            int cumulativeY = 0;
            for (int i = 0; i <= rowHeights.length; i++) {
                g.fillRect(0, cumulativeY, getWidth(), borderThickness);
                if (i < rowHeights.length)
                    cumulativeY += rowHeights[i] + borderThickness;
            }

            // Then column borders.
            int cumulativeX = 0;
            for (int j = 0; j <= columnWidths.length; j++) {
                g.fillRect(cumulativeX, 0, borderThickness, getHeight());
                if (j < columnWidths.length)
                    cumulativeX += columnWidths[j] + borderThickness;
            }
        }
    }
}