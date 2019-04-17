import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

public class TextEditor extends JPanel {
	private JTextArea textArea;
	private JButton openButton;
	private JButton saveButton;
	private JButton findButton;
	private JButton replaceButton;
	private JTextField searchField;
	private JTextField replaceField;
	private JComboBox sizeCombo;
	private JComboBox fontCombo;
	private int highlightPos = 0;
	private String currentWord;
	private boolean found = false; // private member of find

	/**
	 * Consturctor of the class
	 */
	public TextEditor() {
		createComponents();
		wireComponents();
	}

	/**
	 * Create and initialize the variables
	 */
	public void createComponents() {
		setLayout(new BorderLayout());
		// create the text area
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane pane = new JScrollPane(textArea);
		add(pane, BorderLayout.CENTER);

		// create buttons
		openButton = new JButton("Open");
		saveButton = new JButton("Save");
		findButton = new JButton("Find");
		replaceButton = new JButton("Replace");
		searchField = new JTextField();
		replaceField = new JTextField();
		sizeCombo = new JComboBox();
		for (int i = 8; i <= 40; i++) {
			sizeCombo.addItem(i);
			i++;
		}
		sizeCombo.setSelectedIndex(2);
		fontCombo = new JComboBox();
		fontCombo.addItem("Plain");
		fontCombo.addItem("Bold");
		fontCombo.addItem("Italics");

		JPanel controlPanel = new JPanel(new GridLayout(2, 4));
		controlPanel.add(openButton);
		controlPanel.add(sizeCombo);
		controlPanel.add(searchField);
		controlPanel.add(findButton);
		controlPanel.add(saveButton);
		controlPanel.add(fontCombo);
		controlPanel.add(replaceField);
		controlPanel.add(replaceButton);
		add(controlPanel, BorderLayout.SOUTH);
		setLabelFont(); // Initialize the font size and style

	}

	/**
	 * Wire the buttons and the functions
	 */
	public void wireComponents() {
		class ButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLabelFont();

				JFileChooser chooser = new JFileChooser();
				if (e.getSource() == openButton) {
					int returnVal = chooser.showOpenDialog(null);

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						textArea.setText(""); // Empty text area before open new file
						// Read from file to text area
						File inFile = chooser.getSelectedFile();
						try {
							Scanner in = new Scanner(inFile);
							while (in.hasNextLine()) {
								textArea.append(in.nextLine() + '\n');
							}
							in.close();
						} catch (IOException error) {
							JOptionPane.showMessageDialog(null, "Invalid File!", "Warning",
									JOptionPane.WARNING_MESSAGE);
						}
					}
				} else if (e.getSource() == saveButton) {
					int returnVal = chooser.showSaveDialog(null);
					// Start writing to a file only if the user decides to save it
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						// Write from file to text area
						File file = chooser.getSelectedFile();
						try {
							PrintWriter out = new PrintWriter(file);
							// prints one line at a time so it has line break when view in Notepad
							for (String line : textArea.getText().split("\n")) {
								out.println(line);
							}
							out.close();
							JOptionPane.showMessageDialog(null, "File has sucessfully saved!", "Message",
									JOptionPane.INFORMATION_MESSAGE);

						} catch (IOException error) {
							JOptionPane.showMessageDialog(null, "Invalid File!", "Warning",
									JOptionPane.WARNING_MESSAGE);
						}
					}
				} else if (e.getSource() == findButton) {
					highlightPos = highlight(textArea, searchField.getText(), highlightPos); // highlights a word and
																								// update position
					currentWord = searchField.getText();
				} else if (e.getSource() == replaceButton) {
					replace(textArea, replaceField.getText());
				}
			}
		}
		ButtonListener lis = new ButtonListener();
		openButton.addActionListener(lis);
		saveButton.addActionListener(lis);
		findButton.addActionListener(lis);
		replaceButton.addActionListener(lis);
		fontCombo.addActionListener(lis);
		sizeCombo.addActionListener(lis);
	}

	/**
	 * Sets the label of the front sizes and style
	 */
	public void setLabelFont() {
		String fontStyle = (String) fontCombo.getSelectedItem();
		int style = 0;
		if (fontStyle.equals("Plain")) {
			style = Font.PLAIN;
		} else if (fontStyle.equals("Bold")) {
			style = Font.BOLD;
		} else if (fontStyle.equals("Italics")) {
			style = Font.ITALIC;
		}
		int size = (int) sizeCombo.getSelectedItem();
		textArea.setFont(new Font(null, style, size));
		textArea.repaint();
	}

	/**
	 * Highlight the words
	 * 
	 * @param textComp: text component
	 * @param word: a word in string
	 * @param pos: Current position
	 * @return the current position
	 */
	public int highlight(JTextComponent textComp, String word, int pos) {

		removeHighlight(textComp);

		try {
			Highlighter h = textComp.getHighlighter();
			Document doc = textComp.getDocument();
			String text = doc.getText(0, doc.getLength());

			if (!word.equals(currentWord)) {
				pos = 0;
			}

			pos = text.toUpperCase().indexOf(word.toUpperCase(), pos);
			if (pos >= 0) {
				h.addHighlight(pos, pos + word.length(), DefaultHighlighter.DefaultPainter);
				if (pos >= (textComp.getText().length())) { // Reset position when reaching the end of file
					pos = 0;
				} else {
					pos += word.length();
				}
				found = true;
			} else if (!found) {
				JOptionPane.showMessageDialog(null, word + " was not found in the document.", "Text not found",
						JOptionPane.INFORMATION_MESSAGE);
				found = false;
			} else {
				JOptionPane.showMessageDialog(null, " End of the document!", "Search Result",
						JOptionPane.INFORMATION_MESSAGE);
				found = false;
			}
		} catch (BadLocationException error) {
			error.printStackTrace();
		}

		return pos;
	}

	/**
	 * Removes the highlighted word
	 * @param textComp: the text component
	 */
	public void removeHighlight(JTextComponent textComp) {
		Highlighter h = textComp.getHighlighter();
		Highlighter.Highlight[] ht = h.getHighlights();

		for (int i = 0; i < ht.length; i++) {
			if (ht[i].getPainter() instanceof HighlightPainter) {
				h.removeHighlight(ht[i]);
			}
		}
	}

	/**
	 *  Replace certain words in the search field
	 * @param textComp text component
	 * @param word the words to be replaced
	 */
	public void replace(JTextComponent textComp, String word) {
		String doc = textComp.getText();
		String searchWord = searchField.getText();
		String replaceWord = replaceField.getText();

		// Replace all words contain in search field (case-insensitive)
		if (doc.toLowerCase().contains(searchWord.toLowerCase())) {
			textComp.setText(doc.replaceAll("(?i)" + searchWord, replaceWord));
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Text Editor");
		frame.add(new TextEditor());
		frame.setPreferredSize(new Dimension(600, 600));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Prompt the user on exiting
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to close this application?",
						"Confirm Exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (option == JOptionPane.OK_OPTION) {
					e.getWindow().dispose();
				}
			}
		});
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
