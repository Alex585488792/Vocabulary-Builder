import javax.swing.*;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.event.*;
import java.awt.*;

public class Main extends JFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	private JPanel wordPanel, statPanel, reviewPanel, dictPanel;
	private JButton btnAddWord, btnEditWord, btnDeleteWord, btnSearch, btnAddtoList, btnReviewAnswer, btnRemember,
			btnDontRemember;
	private DefaultTableModel modelWord;
	public DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private BufferedWriter out;
	private File wordFile = new File("words.txt"), statFile = new File("stats.txt");
	private int readFileIndex = 0, wordsToBeRevised = 0, wordsToBeRevised2 = 0, rwlindex = 0, wordListIndex = 0;
	private Object value, valueMeaning;
	private JTable wordTable;
	private JTextField wordTF, phoneticSymTF, wordSearchTF;
	private JTextArea meaningTA, searchResultsTA, answerTA, statTA;
	private JLabel wordReview, lblNoOfWordsRemaining;
	private JTabbedPane tabbedPane;
	private JScrollPane meaningTAScroll;
	private ArrayList<Word> wordList = new ArrayList<Word>(), reviseWordList = new ArrayList<Word>();
	private int[] noOfWordsInLevel = new int[6];
	String fieldDelimit = "|";
	String meanDelimit = "^";

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		// main panels
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		wordPanel = new JPanel();
		wordPanel.setPreferredSize(new Dimension(500, 400));
		statPanel = new JPanel();
		reviewPanel = new JPanel();
		reviewPanel.setLayout(null);
		dictPanel = new JPanel();

		// word panel JComponents
		// JTable
		String[] wordColumnTitle = { "ID", "Word", "<html>Phonetic<br> Symbol", "Meaning", "<html>Level of <br>completion",
				"Date Added" };
		modelWord = new DefaultTableModel(1000, wordColumnTitle.length);
		modelWord.setColumnIdentifiers(wordColumnTitle);
		wordTable = new JTable(modelWord) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		wordTable.getColumnModel().getColumn(0).setMinWidth(50);
		wordTable.getColumnModel().getColumn(1).setMinWidth(100);
		wordTable.getColumnModel().getColumn(2).setMinWidth(100);
		wordTable.getColumnModel().getColumn(3).setMinWidth(400);
		wordTable.getColumnModel().getColumn(4).setMinWidth(100);
		wordTable.getColumnModel().getColumn(5).setMinWidth(100);
		wordTable.setPreferredScrollableViewportSize(new Dimension(800, 600));
		wordTable.setFillsViewportHeight(true);
		wordTable.setPreferredSize(null);
		wordFileToArray(wordFile);
		wordArrayToTable(wordList);

		// set header height
		JTableHeader header = wordTable.getTableHeader();
		header.setPreferredSize(new Dimension(100, 40));

		wordTF = new JTextField(15);
		phoneticSymTF = new JTextField(15);
		meaningTA = new JTextArea(3, 3);
		meaningTA.setLineWrap(true);
		meaningTA.setWrapStyleWord(true);
		meaningTAScroll = new JScrollPane(meaningTA);
		meaningTAScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		btnAddWord = new JButton("Add");
		btnAddWord.setFocusable(false);
		btnAddWord.addActionListener(this);
		btnEditWord = new JButton("Edit");
		btnEditWord.setFocusable(false);
		btnEditWord.addActionListener(this);
		btnDeleteWord = new JButton("Delete");
		btnDeleteWord.setFocusable(false);
		btnDeleteWord.addActionListener(this);

		// Statistics panel JComponents
		JLabel statLabel = new JLabel("STATSTICS");
		statLabel.setFont(new Font("Times new Roman", Font.BOLD, 72));
		statLabel.setAlignmentX(CENTER_ALIGNMENT);
		statTA = new JTextArea(20, 45);
		statTA.setEditable(false);
		statTA.setFont(new Font("Times new Roman", Font.PLAIN, 24));
		statFileToArray(statFile);

		// Dictionary panel JComponents
		JLabel dictionaryLabel = new JLabel("DICTIONARY");
		dictionaryLabel.setFont(new Font("Times new Roman", Font.BOLD, 72));
		dictionaryLabel.setAlignmentX(CENTER_ALIGNMENT);
		wordSearchTF = new JTextField(30);
		wordSearchTF.setMinimumSize(new Dimension(600, 50));
		wordSearchTF.setPreferredSize(new Dimension(600, 50));
		wordSearchTF.setFont(new Font("Calibri", Font.PLAIN, 24));
		wordSearchTF.setAlignmentX(CENTER_ALIGNMENT);
		searchResultsTA = new JTextArea();
		searchResultsTA.setFont(new Font("MS Song", Font.PLAIN, 24));
		searchResultsTA.setPreferredSize(new Dimension(750, 500));
		searchResultsTA.setMaximumSize(new Dimension(700, 500));
		btnSearch = new JButton("Search");
		btnSearch.addActionListener(this);
		btnAddtoList = new JButton("Add to list");
		btnAddtoList.setPreferredSize(new Dimension(250, 30));
		btnAddtoList.addActionListener(this);
		btnAddtoList.setEnabled(false);

		// Review panel JComponents
		JLabel reviewQ = new JLabel("What is the meaning of this word?");
		reviewQ.setFont(new Font("Times new Roman", Font.PLAIN, 56));
		reviewQ.setBounds(50, 0, 900, 60);
		lblNoOfWordsRemaining = new JLabel("0");
		lblNoOfWordsRemaining.setBounds(850, 0, 50, 30);
		wordReview = new JLabel("");
		wordReview.setFont(new Font("Times new Roman", Font.BOLD, 72));
		wordReview.setHorizontalAlignment(JLabel.CENTER);
		wordReview.setBounds(0, 70, 900, 100);
		btnReviewAnswer = new JButton("Show answer");
		btnReviewAnswer.setFont(new Font("Times new Roman", Font.BOLD, 72));
		btnReviewAnswer.setPreferredSize(new Dimension(800, 400));
		btnReviewAnswer.setBounds(50, 180, 800, 400);
		btnReviewAnswer.addActionListener(this);
		if (wordsToBeRevised == 0) {
			btnReviewAnswer.setEnabled(false);
		}
		answerTA = new JTextArea();
		answerTA.setBounds(50, 180, 800, 400);
		answerTA.setFont(new Font("Arial", Font.PLAIN, 24));
		btnRemember = new JButton("I Remember");
		btnRemember.setBackground(Color.GREEN);
		btnRemember.setFont(new Font("Arial", Font.PLAIN, 36));
		btnRemember.setBounds(50, 600, 380, 100);
		btnRemember.setEnabled(false);
		btnRemember.addActionListener(this);
		btnDontRemember = new JButton("I Don't Remember");
		btnDontRemember.setBackground(Color.RED);
		btnDontRemember.setFont(new Font("Arial", Font.PLAIN, 36));
		btnDontRemember.setBounds(470, 600, 380, 100);
		btnDontRemember.setEnabled(false);
		btnDontRemember.addActionListener(this);
		// may need fixing here
		if (reviseWordList != null) {
			wordReview.setText(reviseWordList.get(0).getName());
			answerTA.setText(reviseWordList.get(0).getMeaning(0));
		}

		wordPanel.add(btnAddWord);
		wordPanel.add(btnEditWord);
		wordPanel.add(btnDeleteWord);
		wordPanel.add(new JScrollPane(wordTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		statPanel.add(statLabel);
		statPanel.add(statTA);
		dictPanel.add(dictionaryLabel);
		dictPanel.add(Box.createVerticalStrut(20));
		dictPanel.add(wordSearchTF);
		dictPanel.add(btnSearch);
		dictPanel.add(btnAddtoList);
		dictPanel.add(Box.createVerticalStrut(20));
		dictPanel.add(searchResultsTA);
		reviewPanel.add(reviewQ);
		reviewPanel.add(lblNoOfWordsRemaining);
		reviewPanel.add(wordReview);
		reviewPanel.add(btnReviewAnswer);
		reviewPanel.add(answerTA);
		reviewPanel.add(btnRemember);
		reviewPanel.add(btnDontRemember);
		tabbedPane.addTab("Word", wordPanel);
		tabbedPane.addTab("Review", reviewPanel);
		tabbedPane.addTab("Stats", statPanel);
		tabbedPane.addTab("Dictionary", dictPanel);

		setTitle("Vocabulary Builder");
		add(tabbedPane);
		setSize(900, 800);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAddWord) {
			wordTF.setEnabled(true);
			wordTF.setText("");
			phoneticSymTF.setText("");
			meaningTA.setText("");
			Border border = BorderFactory.createLineBorder(Color.BLACK);
			meaningTA.setBorder(BorderFactory.createCompoundBorder(border,
					BorderFactory.createEmptyBorder(10, 10, 10, 10)));
			Object[] addingWords = { "Word:", wordTF, "Phonetic symbol",
					phoneticSymTF, "Meaning:", meaningTAScroll };
			int option = JOptionPane.showConfirmDialog(null, addingWords,
					"Add a word", JOptionPane.OK_CANCEL_OPTION);
			String strWord = wordTF.getText();
			String phoneticSymbol = phoneticSymTF.getText();
			String [] meaning = meaningTA.getText().split("\n");
			if (option == 0) {
				if (strWord.length() == 0 || meaning.length == 0) {
					JOptionPane.showMessageDialog(null,
							"You did not input word/meaning!",
							"Vocabulary Builder",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					Word newWord = new Word(strWord, phoneticSymbol, meaning, 
							0, df.format(new Date()), null, false);
					wordList.add(newWord);
					wordArrayToTable(wordList);
					wordArrayToFile(wordList);
				}
			}
		}
		else if (e.getSource() == btnEditWord) {
			readFileIndex = wordTable.getSelectedRow();
			if (readFileIndex >= 0) {
				String [] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + fieldDelimit);
				int wordId = Integer.parseInt(arrId[0]);
				int meaningId = Integer.parseInt(arrId[1]);
				String oldMeaning = wordList.get(wordId).getMeaning(meaningId);
				wordTF.setEnabled(true);
				wordTF.setText(wordList.get(wordId).getName());
				if (modelWord.getValueAt(readFileIndex, 1) != null) {
					phoneticSymTF.setText(wordList.get(wordId).getPhonetic());
				} else {
					phoneticSymTF.setText("");
				}
				meaningTA.setText(oldMeaning);
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				meaningTA.setBorder(BorderFactory.createCompoundBorder(border,
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", wordTF, "Phonetic symbol",
						phoneticSymTF, "Meaning:", meaningTA };
				int option = JOptionPane.showConfirmDialog(null, addingWords,
						"Edit a word", JOptionPane.OK_CANCEL_OPTION);
				String newWord = wordTF.getText();
				String newPhoneticSymbol = phoneticSymTF.getText();
				String newMeaning = meaningTA.getText();
				if (option == 0) {
					if (newWord.length() == 0 || newMeaning.length() == 0) {
						JOptionPane.showMessageDialog(null,
								"You did not input word/meaning!",
								"Vocabulary Builder",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						wordList.get(wordId).setName(newWord);
						wordList.get(wordId).setPhonetic(newPhoneticSymbol);
						wordList.get(wordId).setMeaning(newMeaning, meaningId);
						wordArrayToTable(wordList);
						wordArrayToFile(wordList);
					}
				}
			}
		}
	}

	public void stateChanged(ChangeEvent arg0) {
		if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Review") && wordsToBeRevised > 0) {
			lblNoOfWordsRemaining.setText(String.valueOf(wordsToBeRevised));
			if (wordsToBeRevised == 1) {
				JOptionPane.showMessageDialog(null, "You have " + wordsToBeRevised + " word to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "You have " + wordsToBeRevised + " words to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Stats")) {
		}
	}

	public void wordFileToArray(File f) {
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				String[] data, meaningData;
				line = in.readLine();
				while (line != null) {
					data = line.split("\\|");
					meaningData = data[2].split("\\^");
					Word wordFromFile = new Word(data[0], data[1], meaningData, Integer.parseInt(data[3]), data[4],
							data[5], Boolean.valueOf(data[6]));
					noOfWordsInLevel[wordFromFile.getLevel()]++;
					wordList.add(wordFromFile);
					if (wordFromFile.getRev()) {
						reviseWordList.add(wordFromFile);
					}
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
			}
		}
	}

	public void statFileToArray(File f) {

	}

	public void wordArrayToTable(ArrayList<Word> aw) {
		int insertIndex = 0;
		for (int i = 0; i < aw.size(); i++) {
			Word word = aw.get(i);
			modelWord.setValueAt(word.getName(), insertIndex, 1);
			modelWord.setValueAt(word.getPhonetic(), insertIndex, 2);
			modelWord.setValueAt(word.getLevel(), insertIndex, 4);
			modelWord.setValueAt(word.getDateAdded(), insertIndex, 5);
			for (int j = 0; j < word.getNumMeaning(); j++) {
				modelWord.setValueAt(word.getMeaning(j), insertIndex, 3);
				modelWord.setValueAt(i + "|" + j, insertIndex, 0);
				insertIndex++;
			}
		}
	}
	
	public void wordArrayToFile(ArrayList<Word> aw) {
		if(wordFile.delete()) {
			wordFile = new File("words.txt");
		}
		try {
			out = new BufferedWriter(new FileWriter(wordFile, true));
			for (int i = 0; i < aw.size(); i++) {
				Word word = aw.get(i);
				String meaningToFile = "";
				for (int mi = 0; mi < word.getNumMeaning(); mi++) {
					meaningToFile = meaningToFile.concat(word.getMeaning(mi));
					if (mi != word.getNumMeaning() - 1) {
						meaningToFile = meaningToFile.concat(meanDelimit);
					}
				}
				out.write(word.getName() + fieldDelimit);
				out.write(word.getPhonetic() + fieldDelimit);
				out.write(meaningToFile + fieldDelimit);
				out.write(word.getLevel() + fieldDelimit);
				out.write(word.getDateAdded() + fieldDelimit);
				out.write(word.getDateCompleted() + fieldDelimit);
				out.write(String.valueOf(word.getRev()));
				out.newLine();
			}
			out.close();
		} catch (IOException f) {
			JOptionPane.showMessageDialog(null, f.getMessage()
					+ "!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public Date stringToDate(String s) {
		try {
			System.out.println(s);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			System.out.println(dateFormat.format(s));
			return dateFormat.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.getMessage();
			return null;
		}

	}
}
