import javax.swing.*;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.awt.event.*;
import java.awt.*;

public class Main extends JFrame implements ActionListener, ChangeListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private static final String fieldDelimit = "|";
	private static final String meanDelimit = "^";
	private static final String dateDelimit = "/";
	private JPanel wordPanel, statPanel, reviewPanel, dictPanel;
	private JButton btnAdd, btnEdit, btnDelete, btnSearch, btnAddtoList, btnReviewAnswer, 
	btnRemember, btnDontRemember, btnPickWord, btnSAT, btnTOEFL, btnGRE;
	private DefaultTableModel modelWord, modelExtWordList;
	private BufferedWriter out;
	private File wordFile = new File("words.txt");
	private String wordSearch;
	private ArrayList<String> meaningSearch;
	private int readFileIndex = 0, id = 0, reviseIndex = 0;
	private JTable wordTable, extList;
	private JTextField wordTF, phoneticSymTF, wordSearchTF;
	private JTextArea meaningTA, searchResultsTA, answerTA, statTA;
	private JLabel wordReview, lblNoOfWordsRemaining;
	private JTabbedPane tabbedPane;
	private JScrollPane meaningTAScroll, answerTAScroll;
	private Word wordReviewing;
	private ArrayList<Word> wordList = new ArrayList<Word>(), reviseWordList = new ArrayList<Word>();
	private int[] noOfWordsInLevel = new int[11];
	String[] wordExtColumnTitle = {"Word", "Meaning", "Add Word" };

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
		String[] wordColumnTitle = { "", "Word", "<html>Phonetic<br> Symbol", "Meaning", "Progress",
				"<html>Date <br>Added" };
		modelWord = new DefaultTableModel(100, wordColumnTitle.length);
		modelWord.setColumnIdentifiers(wordColumnTitle);
		wordTable = new JTable(modelWord) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		wordTable.getColumnModel().getColumn(0).setMinWidth(0);
		wordTable.getColumnModel().getColumn(1).setMinWidth(100);
		wordTable.getColumnModel().getColumn(2).setMinWidth(60);
//		wordTable.getColumnModel().getColumn(3).setMinWidth(400);
		wordTable.getColumnModel().getColumn(3).setMinWidth(510);
		wordTable.getColumnModel().getColumn(4).setMinWidth(60);
		wordTable.getColumnModel().getColumn(5).setMinWidth(70);
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

		btnAdd = new JButton("Add");
		btnAdd.setFocusable(false);
		btnAdd.addActionListener(this);
		btnEdit = new JButton("Edit");
		btnEdit.setFocusable(false);
		btnEdit.addActionListener(this);
		btnDelete = new JButton("Delete");
		btnDelete.setFocusable(false);
		btnDelete.addActionListener(this);

		// Statistics panel JComponents
		JLabel statLabel = new JLabel("STATISTICS");
		statLabel.setFont(new Font("Times new Roman", Font.BOLD, 72));
		statLabel.setAlignmentX(CENTER_ALIGNMENT);
		statTA = new JTextArea(20, 45);
		statTA.setEditable(false);
		statTA.setFont(new Font("Times new Roman", Font.PLAIN, 24));

		// Dictionary panel JComponents
		JLabel dictionaryLabel = new JLabel("Type the word and press Search");
		dictionaryLabel.setFont(new Font("Times new Roman", Font.TRUETYPE_FONT, 48));
		dictionaryLabel.setAlignmentX(CENTER_ALIGNMENT);
		wordSearchTF = new JTextField(30);
		wordSearchTF.setMinimumSize(new Dimension(600, 50));
		wordSearchTF.setPreferredSize(new Dimension(600, 50));
		wordSearchTF.setFont(new Font("Times new Roman", Font.PLAIN, 30));
		wordSearchTF.setAlignmentX(CENTER_ALIGNMENT);
		searchResultsTA = new JTextArea();
		searchResultsTA.setFont(new Font("Times new Roman", Font.PLAIN, 24));
		searchResultsTA.setPreferredSize(new Dimension(750, 300));
		searchResultsTA.setMaximumSize(new Dimension(700, 300));
		searchResultsTA.setLineWrap(true);
		searchResultsTA.setWrapStyleWord(true);
		searchResultsTA.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// searchResultsTA.setEditable(false);
		btnSearch = new JButton("Search");
		btnSearch.addActionListener(this);
		btnSearch.addKeyListener(this);
		btnSearch.setFocusable(true);
		btnAddtoList = new JButton("Add to word list");
		btnAddtoList.setPreferredSize(new Dimension(250, 30));
		btnAddtoList.addActionListener(this);
		btnAddtoList.setEnabled(false);
		JLabel orLabel = new JLabel("or");
		orLabel.setPreferredSize(new Dimension(200,200));
		orLabel.setFont(new Font("Times new Roman", Font.TRUETYPE_FONT, 48));
		btnPickWord = new JButton("<html>Pick word<br>from list");
		btnPickWord.setPreferredSize(new Dimension(100,100));
		btnPickWord.addActionListener(this);

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
		btnReviewAnswer = new JButton("Show Meaning");
		btnReviewAnswer.setFont(new Font("Times new Roman", Font.BOLD, 72));
		btnReviewAnswer.setPreferredSize(new Dimension(800, 400));
		btnReviewAnswer.setBounds(50, 180, 800, 400);
		btnReviewAnswer.addActionListener(this);
		if (reviseWordList.size() == 0) {
			btnReviewAnswer.setEnabled(false);
		}
		answerTA = new JTextArea();
		answerTA.setFont(new Font("Times new Roman", Font.PLAIN, 24));
		answerTA.setWrapStyleWord(true);
		answerTA.setLineWrap(true);
		answerTA.setEditable(false);
		answerTAScroll = new JScrollPane(answerTA);
		answerTAScroll.setBounds(50, 180, 800, 400);
		answerTAScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		btnRemember = new JButton("I Remember");
		btnRemember.setBackground(Color.GREEN);
		btnRemember.setFont(new Font("Times new Roman", Font.PLAIN, 36));
		btnRemember.setBounds(50, 600, 380, 100);
		btnRemember.setEnabled(false);
		btnRemember.addActionListener(this);
		btnDontRemember = new JButton("I Don't Remember");
		btnDontRemember.setBackground(Color.RED);
		btnDontRemember.setFont(new Font("Times new Roman", Font.PLAIN, 36));
		btnDontRemember.setBounds(470, 600, 380, 100);
		btnDontRemember.setEnabled(false);
		btnDontRemember.addActionListener(this);
		if (reviseWordList.size() > 0) {
			wordReviewing = reviseWordList.get(reviseIndex);
			wordReview.setText(reviseWordList.get(reviseIndex).getName());
			answerTA.setText(reviseWordList.get(reviseIndex).getMeaning());
		}

		wordPanel.add(btnAdd);
		wordPanel.add(btnEdit);
		wordPanel.add(btnDelete);
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
		dictPanel.add(orLabel);
		dictPanel.add(btnPickWord);
		reviewPanel.add(reviewQ);
		reviewPanel.add(lblNoOfWordsRemaining);
		reviewPanel.add(wordReview);
		reviewPanel.add(btnReviewAnswer);
		reviewPanel.add(answerTAScroll);
		reviewPanel.add(btnRemember);
		reviewPanel.add(btnDontRemember);
		tabbedPane.addTab("Word List", wordPanel);
		tabbedPane.addTab("Add Word", dictPanel);
		tabbedPane.addTab("Stats", statPanel);
		tabbedPane.addTab("Review", reviewPanel);

		setTitle("Vocabulary Builder");
		add(tabbedPane);
		addKeyListener(this);
		setSize(900, 800);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAdd) {
			readFileIndex = wordTable.getSelectedRow();
			int optionWM = 1;
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				Object[] addOptions = { "Add Meaning", "Add Word" };
				optionWM = JOptionPane.showOptionDialog(null, "Add meaning or word?", "Vocabulary Builder",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, addOptions, null);
			}

			if (optionWM == 0) { // add meaning
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + fieldDelimit);
				int wordId = Integer.parseInt(arrId[0]);
				wordTF.setEnabled(false);
				wordTF.setText(wordList.get(wordId).getName());
				phoneticSymTF.setEnabled(false);
				phoneticSymTF.setText(wordList.get(wordId).getPhonetic());
				meaningTA.setText("");
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				meaningTA.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", wordTF, "Phonetic symbol", phoneticSymTF, "Meaning:",
						meaningTAScroll };
				int optionAdd = JOptionPane.showConfirmDialog(null, addingWords, "Add a word",
						JOptionPane.OK_CANCEL_OPTION);
				String[] meaning = meaningTA.getText().split("\n");
				if (optionAdd == 0) {
					if (meaning.length == 0) {
						JOptionPane.showMessageDialog(null, "You did not input meaning!", "Vocabulary Builder",
								JOptionPane.WARNING_MESSAGE);
					} else {
						wordList.get(wordId).addMeaning(meaning);
						wordArrayToTable(wordList);
						wordArrayToFile(wordList);
					}
				}
			} else if (optionWM == 1) { // add word
				wordTF.setEnabled(true);
				wordTF.setText("");
				phoneticSymTF.setText("");
				meaningTA.setText("");
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				meaningTA.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", wordTF, "Phonetic symbol", phoneticSymTF, "Meaning:",
						meaningTAScroll };
				int optionAdd = JOptionPane.showConfirmDialog(null, addingWords, "Add a word",
						JOptionPane.OK_CANCEL_OPTION);
				String strWord = wordTF.getText();
				String phoneticSymbol = phoneticSymTF.getText();
				String[] meaning = meaningTA.getText().split("\n");
				ArrayList<String> meaningArrayList = new ArrayList<String>(
						Arrays.asList(meaningTA.getText().split("\n")));
				if (optionAdd == 0) {
					if (strWord.length() == 0 || meaning.length == 0) {
						JOptionPane.showMessageDialog(null, "You did not input word/meaning!", "Vocabulary Builder",
								JOptionPane.WARNING_MESSAGE);
					} else {
						Word newWord = new Word(id, strWord, phoneticSymbol, meaningArrayList, 0, LocalDate.now(),
								LocalDate.MIN, LocalDate.now().plusDays(1));
						id++;
						wordList.add(newWord);
						wordArrayToTable(wordList);
						wordArrayToFile(wordList);
					}
				}
			}

		} else if (e.getSource() == btnEdit) {
			readFileIndex = wordTable.getSelectedRow();
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + fieldDelimit);
				int wordId = Integer.parseInt(arrId[0]);
				int meaningId = Integer.parseInt(arrId[1]);
				String oldMeaning = wordList.get(wordId).getMeaning(meaningId);
				wordTF.setEnabled(true);
				wordTF.setText(wordList.get(wordId).getName());
				phoneticSymTF.setEnabled(true);
				if (modelWord.getValueAt(readFileIndex, 1) != null) {
					phoneticSymTF.setText(wordList.get(wordId).getPhonetic());
				} else {
					phoneticSymTF.setText("");
				}
				meaningTA.setText(oldMeaning);
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				meaningTA.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", wordTF, "Phonetic symbol", phoneticSymTF, "Meaning:",
						meaningTAScroll };
				int option = JOptionPane.showConfirmDialog(null, addingWords, "Edit a word",
						JOptionPane.OK_CANCEL_OPTION);
				String newWord = wordTF.getText();
				String newPhoneticSymbol = phoneticSymTF.getText();
				String newMeaning = meaningTA.getText();
				if (option == 0) {
					if (newWord.length() == 0 || newMeaning.length() == 0) {
						JOptionPane.showMessageDialog(null, "You did not input word/meaning!", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else if (newMeaning.indexOf("\n") != -1) {
						JOptionPane.showMessageDialog(null,
								"Please edit one meaning at a time (i.e. do not use 'enter'", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else {
						wordList.get(wordId).setName(newWord);
						wordList.get(wordId).setPhonetic(newPhoneticSymbol);
						wordList.get(wordId).setMeaning(newMeaning, meaningId);
						wordArrayToTable(wordList);
						wordArrayToFile(wordList);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please select a row to edit", "Vocabulary Builder",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == btnDelete) {
			readFileIndex = wordTable.getSelectedRow();
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				Object[] deleteOptions = { "Delete Meaning", "Delete Word" };
				int option = JOptionPane.showOptionDialog(null, "Delete meaning or word?", "Vocabulary Builder",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, deleteOptions, null);
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + fieldDelimit);
				int wordId = Integer.parseInt(arrId[0]);

				if (option == 1) { // delete word
					if (reviseWordList.remove(wordList.get(wordId))) {
						if (wordReviewing.equals(wordList.get(wordId))) {
							setNewWord();
						}
						
					}
					for (int i = 0; i < reviseWordList.size(); i++) {
					}
					// if (wordReviewing.getId() == wor) Deleting a word that is
					// in review word list
					wordList.get(wordId).delete();
					clearTableModel(modelWord);
					wordArrayToTable(wordList);
					wordArrayToFile(wordList);
				} else if (option == 0) {
					int meaningId = Integer.parseInt(arrId[1]);
					if (meaningId == 0 && wordList.get(wordId).getNumMeaning() == 1) {
						JOptionPane.showMessageDialog(null, "Every word must have at least 1 meaning.",
								"Vocabulary Builder", JOptionPane.ERROR_MESSAGE);
					} else {
						wordList.get(wordId).removeMeaning(meaningId);
						clearTableModel(modelWord);
						wordArrayToTable(wordList);
						wordArrayToFile(wordList);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please select a row to delete", "Vocabulary Builder",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == btnSearch) {
			searchResultsTA.setText("");
			meaningSearch = new ArrayList<String>();
			btnAddtoList.setEnabled(true);
			wordSearch = "";
			wordSearch = wordSearchTF.getText();
			
			meaningSearch = searchWord(wordSearch, "oxford");
			
			if (meaningSearch != null) {
				for (int i = 0; i < meaningSearch.size(); i++) {
					searchResultsTA.append(i + 1 + ". " + meaningSearch.get(i) + "\n");
				}
			}
		} else if (e.getSource() == btnAddtoList) {
			if (searchResultsTA.getText().equals("") || wordSearch.length() == 0) {
				JOptionPane.showMessageDialog(null, "Word cannot be added, since word/and or meaning is empty",
						"Vocabulary Builder", JOptionPane.WARNING_MESSAGE);
			} else {

				Word newWord = new Word(id, wordSearch, "", meaningSearch, 0, LocalDate.now(), LocalDate.MIN,
						LocalDate.now().plusDays(1));
				id++;
				wordList.add(newWord);
				wordArrayToTable(wordList);
				wordArrayToFile(wordList);
				JOptionPane.showMessageDialog(null, wordSearch + " successfully added to list!", "Vocabulary Builder",
						JOptionPane.INFORMATION_MESSAGE);
				btnAddtoList.setEnabled(false);
			}
		} else if (e.getSource() == btnReviewAnswer) {
			btnReviewAnswer.setVisible(false);
			btnRemember.setEnabled(true);
			btnDontRemember.setEnabled(true);
		} else if (e.getSource() == btnRemember) {
			if (reviseWordList.remove(wordReviewing)) {
				increaseWordLevel(wordReviewing.getId());
			}
			lblNoOfWordsRemaining.setText(String.valueOf(reviseWordList.size()));
			setNewWord();
		} else if (e.getSource() == btnDontRemember) {
			wordReviewing.dontRemember();
			setNewWord();
		}
		else if (e.getSource() == btnPickWord) {
			// pick a word list panel
			dictPanel.removeAll();
			revalidate();
			repaint();
			dictPanel.setLayout(new BoxLayout(dictPanel, BoxLayout.PAGE_AXIS));
			JLabel lblChooseList = new JLabel("Choose a list:");
			lblChooseList.setAlignmentX(Component.CENTER_ALIGNMENT);
			lblChooseList.setFont(new Font("Times new Roman", Font.TRUETYPE_FONT, 48));
			btnSAT = new JButton("SAT");
			btnSAT.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnSAT.setFont(new Font("Times new Roman", Font.PLAIN, 36));
			btnSAT.addActionListener(this);
			btnTOEFL = new JButton("TOEFL");
			btnTOEFL.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnTOEFL.setFont(new Font("Times new Roman", Font.PLAIN, 36));
			btnTOEFL.addActionListener(this);
			btnGRE = new JButton("GRE");
			btnGRE.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnGRE.setFont(new Font("Times new Roman", Font.PLAIN, 36));
			btnGRE.addActionListener(this);
			
			dictPanel.add(lblChooseList);
			dictPanel.add(Box.createVerticalStrut(100));
			dictPanel.add(btnSAT);
			dictPanel.add(Box.createVerticalStrut(100));
			dictPanel.add(btnTOEFL);
			dictPanel.add(Box.createVerticalStrut(100));
			dictPanel.add(btnGRE);
		}
		else if (e.getSource() == btnTOEFL) {
			dictPanel.removeAll();
			revalidate();
			repaint();
			dictPanel = new JPanel(new FlowLayout());
			JLabel lblTOEFL = new JLabel("TOEFL Word List");
			lblTOEFL.setFont(new Font("Times new Roman", Font.TRUETYPE_FONT, 48));
			lblTOEFL.setAlignmentX(Component.CENTER_ALIGNMENT);
			ArrayList<Word> toefl = getTOEFLList(new File("toefl.txt"));
//			modelExtWordList = new DefaultTableModel(3, wordExtColumnTitle.length);
//			modelExtWordList.setColumnIdentifiers(wordExtColumnTitle);
//			extList = new JTable(modelExtWordList) {
//				private static final long serialVersionUID = 1L;
//
//				public boolean isCellEditable(int row, int column) {
//					return false;
//				};
//			};
//			extList.setPreferredScrollableViewportSize(new Dimension(100,100));
//			extList.setMaximumSize(new Dimension(100,100));
//			extList.setFillsViewportHeight(true);
//			extList.setPreferredSize(null);
			String[] wordColumnTitle = { "", "Word", "<html>Phonetic<br> Symbol", "Meaning", "Progress",
			"<html>Date <br>Added" };
	modelWord = new DefaultTableModel(100, wordColumnTitle.length);
	modelWord.setColumnIdentifiers(wordColumnTitle);
	wordTable = new JTable(modelWord) {
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column) {
			return false;
		};
	};
	wordTable.getColumnModel().getColumn(0).setMinWidth(0);
	wordTable.getColumnModel().getColumn(1).setMinWidth(100);
	wordTable.getColumnModel().getColumn(2).setMinWidth(60);
//	wordTable.getColumnModel().getColumn(3).setMinWidth(400);
	wordTable.getColumnModel().getColumn(3).setMinWidth(510);
	wordTable.getColumnModel().getColumn(4).setMinWidth(60);
	wordTable.getColumnModel().getColumn(5).setMinWidth(70);
	wordTable.setPreferredScrollableViewportSize(new Dimension(800, 600));
	wordTable.setFillsViewportHeight(true);
	wordTable.setPreferredSize(null);

			dictPanel.add(lblTOEFL);
			dictPanel.add(new JScrollPane(wordTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		}
	}

	public void stateChanged(ChangeEvent arg0) {
		if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Review") && reviseWordList.size() > 0) {
			lblNoOfWordsRemaining.setText(String.valueOf(reviseWordList.size()));
			if (reviseWordList.size() == 1) {
				JOptionPane.showMessageDialog(null, "You have " + reviseWordList.size() + " word to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "You have " + reviseWordList.size() + " words to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			}

			if (reviseWordList.size() > 0) {
				wordReviewing = reviseWordList.get(reviseIndex);
				wordReview.setText(reviseWordList.get(reviseIndex).getName());
				answerTA.setText(reviseWordList.get(reviseIndex).getMeaning());
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Stats")) {
			statTA.setText("");
			statTA.append("Total Number of Words: " + wordList.size() + "\n");
			int wordsPastWeek = 0;
			for (int i = 0; i < wordList.size(); i++) {
				long diff = Duration
						.between(LocalDate.now().atStartOfDay(), wordList.get(i).getDateAdded().atStartOfDay())
						.toDays();
				if (diff >= -7) {
					wordsPastWeek++;
				}
			}
			statTA.append("Number of new words in past week: " + wordsPastWeek + "\n");
			for (int i = 0; i < noOfWordsInLevel.length; i++) {
				if (i == 10) {
					statTA.append("Number of words mastered: " + noOfWordsInLevel[i] + "\n");
				} else {
					statTA.append("Number of words in level " + i + ": " + noOfWordsInLevel[i] + "\n");
				}
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Word")) {
			clearTableModel(modelWord);
			wordArrayToTable(wordList);
		}
	}

	public void keyPressed(KeyEvent e) {
		// press enter searches word
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {

	}

	public void wordFileToArray(File f) {
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				String[] data;
				line = in.readLine();
				while (line != null) {
					data = line.split("\\|");
					ArrayList<String> meaningData = new ArrayList<String>(Arrays.asList(data[2].split("\\^")));
					if (meaningData.size() == 1 && meaningData.get(0).length() == 0) { // no
																						// meanings
						meaningData = new ArrayList<String>();
					}
					String[] addDateArray = data[4].split(dateDelimit);
					String[] reviseDateArray = data[6].split(dateDelimit);
					String[] completeDateArray = data[5].split(dateDelimit);
					LocalDate la = LocalDate.of(Integer.parseInt(addDateArray[2]), Integer.parseInt(addDateArray[1]),
							Integer.parseInt(addDateArray[0]));
					LocalDate ld = LocalDate.of(Integer.parseInt(reviseDateArray[2]),
							Integer.parseInt(reviseDateArray[1]), Integer.parseInt(reviseDateArray[0]));
					LocalDate lc = LocalDate.of(Integer.parseInt(completeDateArray[2]),
							Integer.parseInt(completeDateArray[1]), Integer.parseInt(completeDateArray[0]));
					Word wordFromFile = new Word(id, data[0], data[1], meaningData, Integer.parseInt(data[3]), la, lc,
							ld);
					id++;
					if (ld.isBefore(LocalDate.now()) || ld.isEqual(LocalDate.now())) {
						reviseWordList.add(wordFromFile);
					}
					noOfWordsInLevel[wordFromFile.getLevel()]++;
					wordList.add(wordFromFile);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
			}
		}
	}


	public void wordArrayToTable(ArrayList<Word> aw) {
		int insertIndex = 0;
		for (int i = 0; i < aw.size(); i++) {
			Word word = aw.get(i);
			if (word.getName() == "") {
				continue;
			}
			modelWord.setValueAt(word.getName(), insertIndex, 1);
			modelWord.setValueAt(word.getPhonetic(), insertIndex, 2);
			if (word.getLevel() >= 10) {
				modelWord.setValueAt("Mastered", insertIndex, 4);
			} else {
				modelWord.setValueAt(word.getLevel() + dateDelimit + (noOfWordsInLevel.length - 1), insertIndex, 4);
			}
			modelWord.setValueAt(word.getDateAdded(), insertIndex, 5);
			for (int j = 0; j < word.getNumMeaning(); j++) {
				modelWord.setValueAt(word.getMeaning(j), insertIndex, 3);
				modelWord.setValueAt(word.getId() + "|" + j, insertIndex, 0);
				insertIndex++;
			}
		}
	}

	public void wordArrayToFile(ArrayList<Word> aw) {
		if (wordFile.delete()) {
			wordFile = new File("words.txt");
		}
		try {
			out = new BufferedWriter(new FileWriter(wordFile, true));
			for (int i = 0; i < aw.size(); i++) {
				Word word = aw.get(i);
				if (word.getName() == "") {
					continue;
				}
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
				out.write(word.getDateAdded().getDayOfMonth() + dateDelimit + word.getDateAdded().getMonthValue()
						+ dateDelimit + word.getDateAdded().getYear() + fieldDelimit);
				out.write(
						word.getDateCompleted().getDayOfMonth() + dateDelimit + word.getDateCompleted().getMonthValue()
								+ dateDelimit + word.getDateCompleted().getYear() + fieldDelimit);
				out.write(word.getDateRevise().getDayOfMonth() + dateDelimit + word.getDateRevise().getMonthValue()
						+ dateDelimit + word.getDateRevise().getYear());
				out.newLine();
			}
			out.close();
		} catch (IOException f) {
			JOptionPane.showMessageDialog(null, f.getMessage() + "!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void clearTableModel(DefaultTableModel tm) {
		for (int i = 0; i < tm.getRowCount(); i++) {
			for (int j = 0; j < tm.getColumnCount(); j++) {
				tm.setValueAt("", i, j);
			}
		}
	}

	public ArrayList<String> searchWord(String word, String dict) {
		ArrayList<String> meanings = new ArrayList<String>();
		try {
			URL url;
			if (dict.equals("oxford")) {
				url = new URL("https://od-api.oxforddictionaries.com:443/api/v1/entries/en/" + word + "/definitions");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestProperty("app_id", "7834b310");
				conn.setRequestProperty("app_key", "82781012e44b2637051c49060569cce5");

				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				
				boolean subsenses = false;
				String output;
				while ((output = br.readLine()) != null) {
					String trimmed = output.trim();
					if (trimmed.contains("subsenses")) {
						subsenses = true;
					} else if (trimmed.equals("]")) {
						subsenses = false;
					}
					if (trimmed.contains("\"definitions\"") && !subsenses) {
						meanings.add(br.readLine().trim().replaceAll("^\"|\"$", ""));
					}
					if (trimmed.contains("lexicalCategory")) {
						String wordType = trimmed.replaceAll("\"lexicalCategory\": \"", "");
						wordType = wordType.replaceAll("\"", "").replaceAll(",", "");
					}
				}

				conn.disconnect();
				return meanings;
			} else if (dict.equals("dict cn")) {
				url = new URL("http://dict.cn/" + word);
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

				String output;
				// TO-DO Search love
				while ((output = in.readLine()) != null) {
					if (output.contains("strong")) {
						System.out.println(output);
						meanings.add(output.trim().replaceAll("^\"|\"$", "")
								.replaceAll("<strong>", "").replaceAll("</strong>", ""));
					}
					
				}
					
				in.close();
				return meanings;
			} else {
				url = new URL("");
				return null;
			}

		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(null, "URL Malformed", "Vocabulary Builder", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Cannot connect to the Internet", "Vocabulary Builder",
					JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Input/Output Error", "Vocabulary Builder", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (RuntimeException e) {
			JOptionPane.showMessageDialog(null, "There are no results for " + word, "Vocabulary Builder",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public void increaseWordLevel(int wid) {
		for (int i = 0; i < wordList.size(); i++) {
			if (wordList.get(i).getId() == wid) {
				wordList.get(i).incLevel();
				wordList.get(i).setNewRevDate();
				wordArrayToTable(wordList);
				wordArrayToFile(wordList);
			}

		}
	}

	public void setNewWord() {
		btnReviewAnswer.setVisible(true);
		btnRemember.setEnabled(false);
		btnDontRemember.setEnabled(false);
		if (reviseWordList.size() > 0) {
			if (reviseIndex >= reviseWordList.size() - 1) {
				reviseIndex = 0;
			} else {
				reviseIndex++;
			}
			wordReviewing = reviseWordList.get(reviseIndex);
			wordReview.setText(reviseWordList.get(reviseIndex).getName());
			answerTA.setText(reviseWordList.get(reviseIndex).getMeaning());
		} else {
			btnReviewAnswer.setEnabled(false);
			wordReview.setText("");
			JOptionPane.showMessageDialog(null, "Congratulations! You have completed this review session!",
					"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
		}

	}
	ArrayList<Word> getTOEFLList(File f) {
		ArrayList<Word> listTOEFL = new ArrayList<Word>();
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				line = in.readLine();
				while (line != null) {
					String[] arrLine = line.split(":");
					Word wordTOEFL = new Word(arrLine[0], arrLine[1]);
					listTOEFL.add(wordTOEFL);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
			}
		}
		return listTOEFL;
	}
}
