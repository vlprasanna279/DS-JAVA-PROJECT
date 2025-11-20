package javaprogram;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class VisualGraphAnalyzer extends JFrame {
    private GraphCanvas canvas;
    private JTextArea outputArea;
    private JRadioButton directedBtn, undirectedBtn, weightedBtn, unweightedBtn;
    private JButton addEdgeBtn, analyzeBtn, clearBtn, undoBtn;
    private JLabel statusLabel;
    private GraphManager graphManager;
    private Stack<GraphAction> actionHistory;

    public VisualGraphAnalyzer() {
        setTitle("Visual Graph Drawing & Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        graphManager = new GraphManager();
        actionHistory = new Stack<>();
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top Control Panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Center: Split between Canvas and Output
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        // Left: Drawing Canvas
        canvas = new GraphCanvas();
        JScrollPane canvasScroll = new JScrollPane(canvas);
        canvasScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE, 2),
            "Drawing Canvas (Click to Add Vertices)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.BLUE
        ));
        splitPane.setLeftComponent(canvasScroll);
        
        // Right: Output Area
        JPanel outputPanel = createOutputPanel();
        splitPane.setRightComponent(outputPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom: Status Bar
        statusLabel = new JLabel("Ready. Click on canvas to add vertices.");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 240, 240));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(230, 240, 255));
        
        // Graph Configuration Panel
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        configPanel.setOpaque(false);
        
        // Graph Type
        configPanel.add(new JLabel("Graph Type:"));
        ButtonGroup typeGroup = new ButtonGroup();
        undirectedBtn = new JRadioButton("Undirected", true);
        directedBtn = new JRadioButton("Directed");
        typeGroup.add(undirectedBtn);
        typeGroup.add(directedBtn);
        configPanel.add(undirectedBtn);
        configPanel.add(directedBtn);
        
        configPanel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Weight Type
        configPanel.add(new JLabel("Weight:"));
        ButtonGroup weightGroup = new ButtonGroup();
        unweightedBtn = new JRadioButton("Unweighted", true);
        weightedBtn = new JRadioButton("Weighted");
        weightGroup.add(unweightedBtn);
        weightGroup.add(weightedBtn);
        configPanel.add(unweightedBtn);
        configPanel.add(weightedBtn);
        
        panel.add(configPanel, BorderLayout.NORTH);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        
        addEdgeBtn = createStyledButton("Add Edge Mode", new Color(100, 180, 255));
        addEdgeBtn.addActionListener(e -> toggleEdgeMode());
        
        analyzeBtn = createStyledButton("Analyze Graph", new Color(100, 220, 100));
        analyzeBtn.addActionListener(e -> analyzeGraph());
        
        undoBtn = createStyledButton("Undo", new Color(200, 150, 255));
        undoBtn.addActionListener(e -> undoLastAction());
        
        clearBtn = createStyledButton("Clear All", new Color(255, 100, 100));
        clearBtn.addActionListener(e -> clearAll());
        
        buttonPanel.add(addEdgeBtn);
        buttonPanel.add(analyzeBtn);
        buttonPanel.add(undoBtn);
        buttonPanel.add(clearBtn);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN.darker(), 2),
            "Graph Analysis Output",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.GREEN.darker()
        ));
        
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(255, 255, 240));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void toggleEdgeMode() {
        canvas.toggleEdgeMode();
        if (canvas.isEdgeMode()) {
            addEdgeBtn.setText("Cancel Edge Mode");
            addEdgeBtn.setBackground(new Color(255, 150, 100));
            statusLabel.setText("Edge Mode: Select two vertices to connect.");
        } else {
            addEdgeBtn.setText("Add Edge Mode");
            addEdgeBtn.setBackground(new Color(100, 180, 255));
            statusLabel.setText("Click on canvas to add vertices.");
        }
    }

    private void undoLastAction() {
        if (actionHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nothing to undo!",
                "Undo",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        GraphAction action = actionHistory.pop();
        
        if (action.type == ActionType.ADD_VERTEX) {
            canvas.removeLastVertex();
            graphManager.removeVertex(action.vertexId);
            statusLabel.setText("Undone: Removed vertex " + action.vertexId);
        } else if (action.type == ActionType.ADD_EDGE) {
            canvas.removeLastEdge();
            graphManager.removeEdge(action.fromVertex, action.toVertex);
            statusLabel.setText("Undone: Removed edge " + action.fromVertex + " -> " + action.toVertex);
        }
        
        canvas.repaint();
    }

    private void resetSelection() {
        canvas.resetSelection();
        statusLabel.setText("Selection reset. Ready for new operation.");
    }

    private void analyzeGraph() {
        if (graphManager.getVertexCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Please create at least one vertex first!",
                "No Graph",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        graphManager.setDirected(directedBtn.isSelected());
        graphManager.setWeighted(weightedBtn.isSelected());
        
        StringBuilder output = new StringBuilder();
        output.append("===============================================\n");
        output.append("       GRAPH ANALYSIS RESULTS\n");
        output.append("===============================================\n\n");
        
        // Basic Properties
        output.append("GRAPH PROPERTIES:\n");
        output.append("-----------------------------------------------\n");
        output.append(String.format("  * Total Vertices: %d\n", graphManager.getVertexCount()));
        output.append(String.format("  * Total Edges: %d\n", graphManager.getEdgeCount()));
        output.append(String.format("  * Graph Type: %s\n", 
            graphManager.isDirected() ? "Directed" : "Undirected"));
        output.append(String.format("  * Weight Type: %s\n\n", 
            graphManager.isWeighted() ? "Weighted" : "Unweighted"));
        
        // Check which traversals are possible
        output.append("POSSIBLE TRAVERSAL ALGORITHMS:\n");
        output.append("-----------------------------------------------\n");
        output.append("  > BFS (Breadth-First Search) - Possible\n");
        output.append("  > DFS (Depth-First Search) - Possible\n\n");
        
        // Get all vertices sorted
        List<Integer> allVertices = graphManager.getAllVerticesSorted();
        
        // BFS Traversal Paths from all vertices
        if (graphManager.getVertexCount() > 0) {
            output.append("BFS TRAVERSAL PATHS:\n");
            output.append("-----------------------------------------------\n");
            
            for (int startVertex : allVertices) {
                List<Integer> bfsResult = graphManager.bfs(startVertex);
                output.append(String.format("  From Node %d: ", startVertex));
                
                if (bfsResult.isEmpty()) {
                    output.append("No path (isolated vertex)");
                } else {
                    for (int i = 0; i < bfsResult.size(); i++) {
                        output.append(bfsResult.get(i));
                        if (i < bfsResult.size() - 1) output.append(" -> ");
                    }
                }
                output.append("\n");
            }
            output.append("\n");
            
            // DFS Traversal Paths from all vertices
            output.append("DFS TRAVERSAL PATHS:\n");
            output.append("-----------------------------------------------\n");
            
            for (int startVertex : allVertices) {
                List<Integer> dfsResult = graphManager.dfs(startVertex);
                output.append(String.format("  From Node %d: ", startVertex));
                
                if (dfsResult.isEmpty()) {
                    output.append("No path (isolated vertex)");
                } else {
                    for (int i = 0; i < dfsResult.size(); i++) {
                        output.append(dfsResult.get(i));
                        if (i < dfsResult.size() - 1) output.append(" -> ");
                    }
                }
                output.append("\n");
            }
            output.append("\n");
        }
        
        output.append("\n===============================================\n");
        
        outputArea.setText(output.toString());
        outputArea.setCaretPosition(0);
        
        statusLabel.setText("Graph analysis completed successfully!");
    }

    private void clearAll() {
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the entire graph?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            graphManager.clear();
            canvas.clear();
            outputArea.setText("");
            actionHistory.clear();
            statusLabel.setText("Graph cleared. Click on canvas to add vertices.");
        }
    }

    // Inner Class: Graph Canvas
    class GraphCanvas extends JPanel {
        private List<Vertex> vertices;
        private List<Edge> edges;
        private boolean edgeMode;
        private Vertex selectedVertex1;
        private Vertex selectedVertex2;
        private int vertexCounter;

        public GraphCanvas() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
            vertices = new ArrayList<>();
            edges = new ArrayList<>();
            edgeMode = false;
            vertexCounter = 0;
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e);
                }
            });
        }

        private void handleMouseClick(MouseEvent e) {
            if (edgeMode) {
                // Check if clicked on a vertex
                Vertex clicked = findVertexAt(e.getX(), e.getY());
                if (clicked != null) {
                    if (selectedVertex1 == null) {
                        selectedVertex1 = clicked;
                        clicked.setSelected(true);
                        statusLabel.setText("First vertex selected: " + clicked.id + ". Select second vertex.");
                        repaint();
                    } else if (selectedVertex2 == null && clicked != selectedVertex1) {
                        selectedVertex2 = clicked;
                        clicked.setSelected(true);
                        repaint();
                        
                        // Add edge
                        addEdge();
                    }
                }
            } else {
                // Add new vertex
                addVertex(e.getX(), e.getY());
            }
        }

        private void addVertex(int x, int y) {
            vertexCounter++;
            Vertex v = new Vertex(vertexCounter, x, y);
            vertices.add(v);
            graphManager.addVertex(vertexCounter);
            actionHistory.push(new GraphAction(ActionType.ADD_VERTEX, vertexCounter, 0, 0));
            statusLabel.setText("Vertex " + vertexCounter + " added. Total vertices: " + vertices.size());
            repaint();
        }

        private void addEdge() {
            int weight = 1;
            if (weightedBtn.isSelected()) {
                String input = JOptionPane.showInputDialog(
                    VisualGraphAnalyzer.this,
                    "Enter edge weight:",
                    "Edge Weight",
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (input == null || input.trim().isEmpty()) {
                    resetSelection();
                    return;
                }
                
                try {
                    weight = Integer.parseInt(input.trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(VisualGraphAnalyzer.this,
                        "Invalid weight. Using default weight 1.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                    weight = 1;
                }
            }
            
            Edge edge = new Edge(selectedVertex1, selectedVertex2, weight);
            edges.add(edge);
            graphManager.addEdge(selectedVertex1.id, selectedVertex2.id, weight);
            
            statusLabel.setText("Edge added: " + selectedVertex1.id + " → " + selectedVertex2.id + 
                (weightedBtn.isSelected() ? " (weight: " + weight + ")" : ""));
            
            resetSelection();
        }

        public void toggleEdgeMode() {
            edgeMode = !edgeMode;
            if (!edgeMode) {
                resetSelection();
            }
        }

        public boolean isEdgeMode() {
            return edgeMode;
        }

        public void resetSelection() {
            if (selectedVertex1 != null) selectedVertex1.setSelected(false);
            if (selectedVertex2 != null) selectedVertex2.setSelected(false);
            selectedVertex1 = null;
            selectedVertex2 = null;
            if (edgeMode) {
                statusLabel.setText("Edge Mode: Select two vertices to connect.");
            }
            repaint();
        }

        private Vertex findVertexAt(int x, int y) {
            for (Vertex v : vertices) {
                if (v.contains(x, y)) {
                    return v;
                }
            }
            return null;
        }

        public void removeLastVertex() {
            if (!vertices.isEmpty()) {
                vertices.remove(vertices.size() - 1);
                vertexCounter--;
            }
        }

        public void removeLastEdge() {
            if (!edges.isEmpty()) {
                edges.remove(edges.size() - 1);
            }
        }

        public void clear() {
            vertices.clear();
            edges.clear();
            vertexCounter = 0;
            edgeMode = false;
            selectedVertex1 = null;
            selectedVertex2 = null;
            addEdgeBtn.setText("Add Edge Mode");
            addEdgeBtn.setBackground(new Color(100, 180, 255));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw edges first
            for (Edge edge : edges) {
                drawEdge(g2d, edge);
            }
            
            // Draw vertices on top
            for (Vertex vertex : vertices) {
                drawVertex(g2d, vertex);
            }
        }

        private void drawEdge(Graphics2D g2d, Edge edge) {
            Vertex v1 = edge.start;
            Vertex v2 = edge.end;
            
            // Calculate edge endpoints
            int x1 = v1.x;
            int y1 = v1.y;
            int x2 = v2.x;
            int y2 = v2.y;
            
            // Draw line
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x1, y1, x2, y2);
            
            // Draw arrow for directed graph
            if (directedBtn.isSelected()) {
                drawArrow(g2d, x1, y1, x2, y2);
            }
            
            // Draw weight if weighted
            if (weightedBtn.isSelected()) {
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                g2d.setColor(Color.RED);
                g2d.fillOval(midX - 10, midY - 10, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String weightStr = String.valueOf(edge.weight);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(weightStr);
                g2d.drawString(weightStr, midX - textWidth / 2, midY + 5);
            }
        }

        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowLength = 15;
            
            // Calculate arrow point (near the destination vertex)
            double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            double ratio = (distance - 25) / distance; // 25 pixels before vertex center
            int arrowX = (int) (x1 + (x2 - x1) * ratio);
            int arrowY = (int) (y1 + (y2 - y1) * ratio);
            
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            
            xPoints[0] = arrowX;
            yPoints[0] = arrowY;
            xPoints[1] = (int) (arrowX - arrowLength * Math.cos(angle - Math.PI / 6));
            yPoints[1] = (int) (arrowY - arrowLength * Math.sin(angle - Math.PI / 6));
            xPoints[2] = (int) (arrowX - arrowLength * Math.cos(angle + Math.PI / 6));
            yPoints[2] = (int) (arrowY - arrowLength * Math.sin(angle + Math.PI / 6));
            
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        private void drawVertex(Graphics2D g2d, Vertex vertex) {
            // Draw circle
            if (vertex.isSelected()) {
                g2d.setColor(new Color(255, 200, 0));
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(new Color(100, 150, 255));
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.fillOval(vertex.x - 20, vertex.y - 20, 40, 40);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(vertex.x - 20, vertex.y - 20, 40, 40);
            
            // Draw ID
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String idStr = String.valueOf(vertex.id);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(idStr);
            int textHeight = fm.getAscent();
            g2d.drawString(idStr, vertex.x - textWidth / 2, vertex.y + textHeight / 3);
        }
    }

    // Inner Classes
    class Vertex {
        int id;
        int x, y;
        boolean selected;

        public Vertex(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.selected = false;
        }

        public boolean contains(int px, int py) {
            int dx = px - x;
            int dy = py - y;
            return (dx * dx + dy * dy) <= 400; // radius = 20
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }
    }

    class Edge {
        Vertex start, end;
        int weight;

        public Edge(Vertex start, Vertex end, int weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new VisualGraphAnalyzer());
    }
}

// Action tracking for undo functionality
enum ActionType {
    ADD_VERTEX,
    ADD_EDGE
}

class GraphAction {
    ActionType type;
    int vertexId;
    int fromVertex;
    int toVertex;

    public GraphAction(ActionType type, int vertexId, int fromVertex, int toVertex) {
        this.type = type;
        this.vertexId = vertexId;
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }
}

// Graph Manager with Adjacency List
class GraphManager {
    private Map<Integer, List<EdgeInfo>> adjacencyList;
    private Set<Integer> vertices;
    private boolean isDirected;
    private boolean isWeighted;
    private int edgeCount;

    public GraphManager() {
        adjacencyList = new HashMap<>();
        vertices = new HashSet<>();
        isDirected = false;
        isWeighted = false;
        edgeCount = 0;
    }

    public void addVertex(int id) {
        vertices.add(id);
        adjacencyList.putIfAbsent(id, new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        adjacencyList.get(from).add(new EdgeInfo(to, weight));
        if (!isDirected) {
            adjacencyList.get(to).add(new EdgeInfo(from, weight));
        }
        edgeCount++;
    }

    public void removeVertex(int id) {
        vertices.remove(id);
        adjacencyList.remove(id);
        // Remove all edges connected to this vertex
        for (List<EdgeInfo> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.dest == id);
        }
    }

    public void removeEdge(int from, int to) {
        List<EdgeInfo> edges = adjacencyList.get(from);
        if (edges != null) {
            edges.removeIf(edge -> edge.dest == to);
        }
        if (!isDirected) {
            List<EdgeInfo> reverseEdges = adjacencyList.get(to);
            if (reverseEdges != null) {
                reverseEdges.removeIf(edge -> edge.dest == from);
            }
        }
        edgeCount--;
    }

    public List<Integer> bfs(int start) {
        List<Integer> result = new ArrayList<>();
        if (!vertices.contains(start)) return result;
        
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        
        visited.add(start);
        queue.offer(start);
        
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            result.add(vertex);
            
            List<EdgeInfo> edges = adjacencyList.get(vertex);
            if (edges != null) {
                for (EdgeInfo edge : edges) {
                    if (!visited.contains(edge.dest)) {
                        visited.add(edge.dest);
                        queue.offer(edge.dest);
                    }
                }
            }
        }
        
        return result;
    }

    public List<Integer> dfs(int start) {
        List<Integer> result = new ArrayList<>();
        if (!vertices.contains(start)) return result;
        
        Set<Integer> visited = new HashSet<>();
        dfsHelper(start, visited, result);
        return result;
    }

    private void dfsHelper(int vertex, Set<Integer> visited, List<Integer> result) {
        visited.add(vertex);
        result.add(vertex);
        
        List<EdgeInfo> edges = adjacencyList.get(vertex);
        if (edges != null) {
            for (EdgeInfo edge : edges) {
                if (!visited.contains(edge.dest)) {
                    dfsHelper(edge.dest, visited, result);
                }
            }
        }
    }

    public boolean isConnected() {
        if (vertices.isEmpty()) return true;
        
        int startVertex = vertices.iterator().next();
        List<Integer> reachable = bfs(startVertex);
        return reachable.size() == vertices.size();
    }

    public int countConnectedComponents() {
        Set<Integer> visited = new HashSet<>();
        int components = 0;
        
        for (int vertex : vertices) {
            if (!visited.contains(vertex)) {
                components++;
                List<Integer> component = bfs(vertex);
                visited.addAll(component);
            }
        }
        
        return components;
    }

    public List<Integer> getAllVerticesSorted() {
        List<Integer> sorted = new ArrayList<>(vertices);
        Collections.sort(sorted);
        return sorted;
    }

    public void clear() {
        adjacencyList.clear();
        vertices.clear();
        edgeCount = 0;
    }

    public int getVertexCount() { return vertices.size(); }
    public int getEdgeCount() { return edgeCount; }
    public boolean isDirected() { return isDirected; }
    public boolean isWeighted() { return isWeighted; }
    public void setDirected(boolean directed) { this.isDirected = directed; }
    public void setWeighted(boolean weighted) { this.isWeighted = weighted; }
}

class EdgeInfo {
    int dest;
    int weight;

    public EdgeInfo(int dest, int weight) {
        this.dest = dest;
        this.weight = weight;
    }
}