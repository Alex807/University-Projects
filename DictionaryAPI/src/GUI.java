import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import components.OnlineDictionary;

public class GUI extends JFrame {
    
    // gui components
    private JButton submitButton;
    private JButton exitButton;
    private JButton clearButton;
    private JTextField textField1; 
    private JTextField textField2;
    private JLabel label1; 
    private JLabel label2;
    private JLabel countLabel;
    private JPanel mainPanel;
    private JComboBox<String> languageHistory;
    
    // less harsh colors
    private final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    private final Color HEADER_COLOR = new Color(90, 110, 150);
    private final Color BUTTON_SUBMIT_COLOR = new Color(100, 150, 120); // softer green
    private final Color BUTTON_CLEAR_COLOR = new Color(110, 140, 180);  // softer blue
    private final Color BUTTON_EXIT_COLOR = new Color(180, 120, 120);   // softer red
    private final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private final Color LABEL_COLOR = new Color(70, 70, 90);
    
    // language history
    private LinkedList<String> recentLanguages = new LinkedList<>();
    private DefaultComboBoxModel<String> languageModel = new DefaultComboBoxModel<>();
    
    // dictionary reference to get entry count
    private OnlineDictionary dictionary;
    
    public GUI() {
        // Set up the frame
        setTitle("Online Dictionary API");
        setSize(570, 410); // adjusted size to match image
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // initialize dictionary
        dictionary = new OnlineDictionary("../resources/DataBase.txt", ":");
        
        // Create main panel with a gradient background
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR, 0, h, new Color(235, 235, 240));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Online Dictionary");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Create input fields with improved styling - centered
        JPanel inputPanel1 = createInputPanel("Enter searched word:", 1);
        JPanel inputPanel2 = createLanguageInputPanel("Enter source_language(in English):");
        
        // center input panels
        inputPanel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add input panels to content
        contentPanel.add(inputPanel1);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(inputPanel2);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // add label for displaying entry count
        countLabel = new JLabel("Words in dictionar: " + getDictionarySize());
        countLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        countLabel.setForeground(LABEL_COLOR);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(countLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Create button panel with fixed size
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 0)); // grid layout for equal buttons
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(350, 45));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create styled buttons
        submitButton = createStyledButton("Submit", BUTTON_SUBMIT_COLOR);
        clearButton = createStyledButton("Clear", BUTTON_CLEAR_COLOR);
        exitButton = createStyledButton("Exit", BUTTON_EXIT_COLOR);
        
        // Add buttons to panel
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);
        
        // Add button panel to content
        contentPanel.add(buttonPanel);
        
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        setContentPane(mainPanel);
        
        // Add action listeners
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllFields();
            }
        });
        
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textField1.requestFocus(); // move cursor to first field automatically when submitting
                String word = textField1.getText();
                String language = textField2.getText(); 
                
                if (word.isEmpty() || language.isEmpty()) {
                    showErrorMessage("Please fill in all fields");
                    return;
                }
                
                // add language to history
                addLanguageToHistory(language);
                
                // Call method from Dictionary class
                String result = dictionary.getInformationsAbout(word, language);
                
                // update count label
                updateCountLabel();
                
                // Display result in a styled dialog
                showResultDialog(result, word);
                
                // Auto-clear fields after showing result
                clearFields();

            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showGoodbyeDialog();
            }
        });
        
        // listener for combobox
        languageHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedLanguage = (String) languageHistory.getSelectedItem();
                if (selectedLanguage != null && !selectedLanguage.isEmpty()) {
                    textField2.setText(selectedLanguage);
                }
            }
        });
    }
    
    private JPanel createInputPanel(String labelText, int fieldNumber) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(LABEL_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setMaximumSize(new Dimension(300, 35));
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        if (fieldNumber == 1) {
            textField1 = textField;
        } else {
            textField2 = textField;
        }
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(textField);
        
        return panel;
    }
    
    private JPanel createLanguageInputPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(LABEL_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        textField2 = new JTextField(15);
        textField2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField2.setMaximumSize(new Dimension(300, 35));
        textField2.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField2.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // create combobox for language history - with placeholder
        languageModel = new DefaultComboBoxModel<String>() {
            private boolean selectingItem = false;
            
            @Override
            public void setSelectedItem(Object item) {
                if (!selectingItem) {
                    selectingItem = true;
                    super.setSelectedItem(item);
                    selectingItem = false;
                }
            }
        };
        
        // add placeholder
        languageModel.addElement("recent used languages");
        
        languageHistory = new JComboBox<>(languageModel);
        languageHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        languageHistory.setMaximumSize(new Dimension(300, 30));
        languageHistory.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // add listener to handle placeholder
        languageHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // check if only placeholder exists
                if (languageModel.getSize() == 1 && 
                    "recent used languages".equals(languageModel.getElementAt(0))) {
                    // do nothing, no recent languages
                } else if (languageModel.getSize() > 0 && 
                        "recent used languages".equals(languageModel.getElementAt(0))) {
                    // remove placeholder, keep only languages
                    languageModel.removeElementAt(0);
                }
            }
        });
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(textField2);
        panel.add(Box.createVerticalStrut(5));
        panel.add(languageHistory);
        
        return panel;
    }

    private void addLanguageToHistory(String language) {
        // check if placeholder exists and remove it
        if (languageModel.getSize() > 0 && 
            "recent used languages".equals(languageModel.getElementAt(0))) {
            languageModel.removeElementAt(0);
        }
        
        // check if language already exists in history
        boolean languageExists = false;
        for (int i = 0; i < languageModel.getSize(); i++) {
            if (language.equals(languageModel.getElementAt(i))) {
                languageExists = true;
                recentLanguages.remove(language);
                break;
            }
        }
        
        // add language to beginning of list
        recentLanguages.addFirst(language);
        
        // keep only last 3 languages
        while (recentLanguages.size() > 3) {
            recentLanguages.removeLast();
        }
        
        // update combobox model
        languageModel.removeAllElements();
        for (String lang : recentLanguages) {
            languageModel.addElement(lang);
        }
        
        // if no languages, add placeholder back
        if (languageModel.getSize() == 0) {
            languageModel.addElement("recent used languages");
        }
    }
    
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
        
        return button;
    }
    
    
    private int getDictionarySize() {
        try {
            return dictionary.getCacheMemorySize();
        } catch (Exception e) {
            // return 0 if method doesn't exist
            return 0;
        }
    }
    
    private void updateCountLabel() {
        countLabel.setText("Words in dictionar: " + getDictionarySize());
    }
    
    private void showResultDialog(String result, String word) {
        JDialog resultDialog = new JDialog(this, "Dictionary Result", true);
        resultDialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(word);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(HEADER_COLOR);
        
        JTextArea resultArea = new JTextArea(result);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton okButton = createStyledButton("OK", BUTTON_CLEAR_COLOR);
        okButton.setPreferredSize(new Dimension(80, 35));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(resultArea, BorderLayout.CENTER); // back to previous layout without JScrollPane
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        resultDialog.add(contentPanel);
        resultDialog.setSize(500, 320);
        resultDialog.setLocationRelativeTo(this);
        
        okButton.addActionListener(e -> resultDialog.dispose());
        
        resultDialog.setVisible(true);
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Input Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showGoodbyeDialog() {
        JDialog exitDialog = new JDialog(this, "", false);
        exitDialog.setUndecorated(true);
        exitDialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel messageLabel = new JLabel("Thank you for using the Dictionary Application!");
        messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel goodbyeLabel = new JLabel("Goodbye!");
        goodbyeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        goodbyeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);
        messagePanel.add(messageLabel);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(goodbyeLabel);
        
        panel.add(messagePanel, BorderLayout.CENTER);
        exitDialog.add(panel, BorderLayout.CENTER);
        
        exitDialog.pack();
        exitDialog.setLocationRelativeTo(this);
        exitDialog.setVisible(true);
        
        // Auto-close dialog and exit application
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            exitDialog.dispose();
            System.exit(0);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void clearAllFields() { 
        textField1.setText("");
        textField2.setText("");
        
        recentLanguages.clear(); // clear language history
        languageModel.removeAllElements();
        languageModel.addElement("recent used languages"); // always add placeholder
        
        // Make sure the combobox shows the placeholder text
        languageHistory.setSelectedItem("recent used languages");
        
        textField2.setText("");
        textField1.requestFocus();
    }


    private void clearFields() {
        textField1.setText("");
        textField1.requestFocus(); 
        //keep previous language typed(high chance of reusing)
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel with custom tweaks
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Customize UI elements
                UIManager.put("TextField.caretForeground", new Color(90, 110, 150));
                UIManager.put("TextField.selectionBackground", new Color(90, 110, 150, 100));
            
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}