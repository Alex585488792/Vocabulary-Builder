import javax.swing.*;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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

public class Main extends JFrame implements ActionListener, ChangeListener, TableModelListener {
	private static final long serialVersionUID = 1L;
	private static final String delimitField = "|", delimitMean = "^", delimitDate = "/";
	private ArrayList<String> meaningSearch;
	private ArrayList<Word> alWord = new ArrayList<Word>(), alRevise = new ArrayList<Word>(), alSAT, alTOEFL, alGRE;
	private BufferedWriter out;
	private DefaultTableModel modelWord, modelExtWordList;
	private File fileWord = new File("resources/words.txt"), fileSAT = new File("resources/sat.txt"), 
			fileTOEFL = new File("resources/toefl.txt"), fileGRE = new File("resources/gre.txt");
	private Font fontTitle = new Font("Times new Roman", Font.TRUETYPE_FONT, 48),
			fontLargeBtn = new Font("Times new Roman", Font.BOLD, 36),
			fontBody = new Font("Times new Roman", Font.PLAIN, 24),
			fontReviewWord = new Font("Times new Roman", Font.BOLD, 96);
	private int readFileIndex = 0, id = 0, reviseIndex = 0, noOfExtWordListWords;
	private int[] noOfWordsInLevel = new int[11];
	private JButton btnAdd, btnEdit, btnDelete, btnSearch, btnAddtoList, btnReviewAnswer, btnRemember, btnDontRemember,
			btnPickWord, btnSAT, btnAddSAT, btnTOEFL, btnAddTOEFL, btnGRE, btnAddGRE, btnBackDict1, btnBackDict2;
	private JLabel lblWordReview, lblNoOfWordsRemaining, lblTypeWord, lblOr;
	private JPanel panelWordList, panelStat, panelReview, panelAddWord;
	private JScrollPane spTAMeaning, spTAAnswer;
	private JTabbedPane tabbedPane;
	private JTable tableWord, tableExtList;
	private JTextArea taMeaning, taSearchResults, taAnswer, taStat;
	private JTextField tfWord, tfPOSSym, tfWordSearch;
	private String wordSearch;
	private String[] toeflColTitle = { "Word", "Meaning", "Add Word" },
			greColTitle = { "Word", "<html>Part of<br>Speech", "Meaning", "Add Word" };
	private Word wordReviewing;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		// main panels
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		panelWordList = new JPanel();
		panelStat = new JPanel();
		panelReview = new JPanel();
		panelReview.setLayout(null);
		panelAddWord = new JPanel();

		// Word List panel JComponents
		// JTable
		String[] wordColumnTitle = { "", "Word", "<html>Part of<br> Speech", "Meaning", "Progress",
				"<html>Date <br>Added" };
		modelWord = new DefaultTableModel(0, wordColumnTitle.length);
		modelWord.setColumnIdentifiers(wordColumnTitle);
		tableWord = new JTable(modelWord) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		tableWord.getColumnModel().getColumn(0).setMinWidth(0);
		tableWord.getColumnModel().getColumn(1).setMinWidth(100);
		tableWord.getColumnModel().getColumn(2).setMinWidth(60);
		tableWord.getColumnModel().getColumn(3).setMinWidth(510);
		tableWord.getColumnModel().getColumn(4).setMinWidth(60);
		tableWord.getColumnModel().getColumn(5).setMinWidth(70);
		tableWord.setPreferredScrollableViewportSize(new Dimension(800, 600));
		tableWord.setFillsViewportHeight(true);
		tableWord.setPreferredSize(null);
		wordFileToArray(fileWord);
		wordArrayToTable(alWord);

		// set header height
		JTableHeader header = tableWord.getTableHeader();
		header.setPreferredSize(new Dimension(100, 40));

		// Add/Edit/Delete JOptionPane components
		tfWord = new JTextField(15);
		tfPOSSym = new JTextField(15);
		taMeaning = new JTextArea(3, 3);
		taMeaning.setLineWrap(true);
		taMeaning.setWrapStyleWord(true);
		spTAMeaning = new JScrollPane(taMeaning);
		spTAMeaning.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		btnAdd = new JButton("Add");
		btnAdd.setFocusable(false);
		btnAdd.addActionListener(this);
		btnAdd.setBackground(Color.GREEN);
		btnEdit = new JButton("Edit");
		btnEdit.setFocusable(false);
		btnEdit.addActionListener(this);
		btnEdit.setBackground(Color.YELLOW);
		btnDelete = new JButton("Delete");
		btnDelete.setFocusable(false);
		btnDelete.addActionListener(this);
		btnDelete.setBackground(Color.RED);

		// Statistics panel JComponents
		JLabel lblStatistics = new JLabel("Statistics");
		lblStatistics.setFont(fontTitle);
		lblStatistics.setAlignmentX(CENTER_ALIGNMENT);
		taStat = new JTextArea(20, 45);
		taStat.setEditable(false);
		taStat.setFont(fontBody);

		// Dictionary panel JComponents
		lblTypeWord = new JLabel("Type the word and press Search");
		lblTypeWord.setFont(fontTitle);
		lblTypeWord.setAlignmentX(CENTER_ALIGNMENT);
		tfWordSearch = new JTextField(30);
		tfWordSearch.setMinimumSize(new Dimension(600, 50));
		tfWordSearch.setPreferredSize(new Dimension(600, 50));
		tfWordSearch.setFont(new Font("Times new Roman", Font.PLAIN, 30));
		tfWordSearch.setAlignmentX(CENTER_ALIGNMENT);
		taSearchResults = new JTextArea();
		taSearchResults.setFont(fontBody);
		taSearchResults.setPreferredSize(new Dimension(750, 300));
		taSearchResults.setMaximumSize(new Dimension(700, 300));
		taSearchResults.setLineWrap(true);
		taSearchResults.setWrapStyleWord(true);
		taSearchResults.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// searchResultsTA.setEditable(false);
		btnSearch = new JButton("Search");
		btnSearch.addActionListener(this);
		btnSearch.setFocusable(true);
		btnSearch.setBackground(Color.CYAN);
		btnAddtoList = new JButton("Add to word list");
		btnAddtoList.setPreferredSize(new Dimension(250, 30));
		btnAddtoList.addActionListener(this);
		btnAddtoList.setEnabled(false);
		btnAddtoList.setBackground(Color.GREEN);
		lblOr = new JLabel("or");
		lblOr.setPreferredSize(new Dimension(200, 200));
		lblOr.setFont(fontTitle);
		btnPickWord = new JButton("<html>Pick word<br>from list");
		btnPickWord.setPreferredSize(new Dimension(100, 100));
		btnPickWord.addActionListener(this);
		btnPickWord.setBackground(Color.CYAN);

		// Review panel JComponents
		JLabel lblWhatMeaning = new JLabel("What is the meaning of this word?");
		lblWhatMeaning.setFont(fontTitle);
		lblWhatMeaning.setBounds(150, 0, 900, 60);
		lblNoOfWordsRemaining = new JLabel("0");
		lblNoOfWordsRemaining.setBounds(850, 0, 50, 30);
		lblWordReview = new JLabel("");
		lblWordReview.setFont(fontReviewWord);
		lblWordReview.setHorizontalAlignment(JLabel.CENTER);
		lblWordReview.setBounds(0, 40, 900, 150);
		btnReviewAnswer = new JButton("Show Meaning");
		btnReviewAnswer.setFont(fontLargeBtn);
		btnReviewAnswer.setPreferredSize(new Dimension(800, 400));
		btnReviewAnswer.setBounds(50, 180, 800, 400);
		btnReviewAnswer.addActionListener(this);
		btnReviewAnswer.setBackground(Color.CYAN);
		if (alRevise.size() == 0) {
			btnReviewAnswer.setEnabled(false);
		}
		taAnswer = new JTextArea();
		taAnswer.setFont(fontBody);
		taAnswer.setWrapStyleWord(true);
		taAnswer.setLineWrap(true);
		taAnswer.setEditable(false);
		spTAAnswer = new JScrollPane(taAnswer);
		spTAAnswer.setBounds(50, 180, 800, 400);
		spTAAnswer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		btnRemember = new JButton("I Remember");
		btnRemember.setBackground(Color.GREEN);
		btnRemember.setFont(fontLargeBtn);
		btnRemember.setBounds(50, 600, 380, 100);
		btnRemember.setEnabled(false);
		btnRemember.addActionListener(this);
		btnDontRemember = new JButton("I Don't Remember");
		btnDontRemember.setBackground(Color.RED);
		btnDontRemember.setFont(fontLargeBtn);
		btnDontRemember.setBounds(470, 600, 380, 100);
		btnDontRemember.setEnabled(false);
		btnDontRemember.addActionListener(this);
		if (alRevise.size() > 0) {
			wordReviewing = alRevise.get(reviseIndex);
			lblWordReview.setText(alRevise.get(reviseIndex).getName());
			taAnswer.setText(alRevise.get(reviseIndex).getMeaning());
		}

		panelWordList.add(btnAdd);
		panelWordList.add(btnEdit);
		panelWordList.add(btnDelete);
		panelWordList.add(new JScrollPane(tableWord, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		panelStat.add(lblStatistics);
		panelStat.add(taStat);
		panelAddWord.add(lblTypeWord);
		panelAddWord.add(Box.createVerticalStrut(20));
		panelAddWord.add(tfWordSearch);
		panelAddWord.add(btnSearch);
		panelAddWord.add(btnAddtoList);
		panelAddWord.add(Box.createVerticalStrut(20));
		panelAddWord.add(taSearchResults);
		panelAddWord.add(lblOr);
		panelAddWord.add(btnPickWord);
		panelReview.add(lblWhatMeaning);
		panelReview.add(lblNoOfWordsRemaining);
		panelReview.add(lblWordReview);
		panelReview.add(btnReviewAnswer);
		panelReview.add(spTAAnswer);
		panelReview.add(btnRemember);
		panelReview.add(btnDontRemember);
		tabbedPane.addTab("Word List", panelWordList);
		tabbedPane.addTab("Add Word", panelAddWord);
		tabbedPane.addTab("Stats", panelStat);
		tabbedPane.addTab("Review", panelReview);

		setTitle("Vocabulary Builder");
		add(tabbedPane);
		setSize(900, 800);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAdd) {
			readFileIndex = tableWord.getSelectedRow();
			int optionWM = 1;
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				Object[] addOptions = { "Add Meaning", "Add Word" };
				optionWM = JOptionPane.showOptionDialog(null, "Add meaning or word?", "Vocabulary Builder",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, addOptions, null);
			}

			if (optionWM == 0) { // add meaning
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + delimitField);
				int wordId = Integer.parseInt(arrId[0]);
				tfWord.setEnabled(false);
				tfWord.setText(alWord.get(wordId).getName());
				tfPOSSym.setEnabled(false);
				tfPOSSym.setText(alWord.get(wordId).getPOS());
				taMeaning.setText("");
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				taMeaning.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", tfWord, "Part of Speech", tfPOSSym, "Meaning:", spTAMeaning };
				int optionAdd = JOptionPane.showConfirmDialog(null, addingWords, "Add a word",
						JOptionPane.OK_CANCEL_OPTION);
				String[] meaning = taMeaning.getText().split("\n");
				if (optionAdd == 0) {
					if (meaning.length == 0) {
						JOptionPane.showMessageDialog(null, "You did not input meaning!", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else {
						alWord.get(wordId).addMeaning(meaning);
						wordArrayToTable(alWord);
						wordArrayToFile(alWord);
					}
				}
			} else if (optionWM == 1) { // add word
				tfWord.setEnabled(true);
				tfWord.setText("");
				tfPOSSym.setText("");
				taMeaning.setText("");
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				taMeaning.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", tfWord, "Part of Speech", tfPOSSym, "Meaning:", spTAMeaning };
				int optionAdd = JOptionPane.showConfirmDialog(null, addingWords, "Add a word",
						JOptionPane.OK_CANCEL_OPTION);
				String strWord = tfWord.getText();
				String pos = tfPOSSym.getText();
				String[] meaning = taMeaning.getText().split("\n");
				ArrayList<String> meaningArrayList = new ArrayList<String>(
						Arrays.asList(taMeaning.getText().split("\n")));
				if (optionAdd == 0) {
					if (strWord.length() == 0 || meaning.length == 0) {
						JOptionPane.showMessageDialog(null, "You did not input word/meaning!", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else {
						Word newWord = new Word(id, strWord, pos, meaningArrayList, 0, LocalDate.now(),
								LocalDate.MIN, LocalDate.now().plusDays(1));
						id++;
						alWord.add(newWord);
						wordArrayToTable(alWord);
						wordArrayToFile(alWord);
					}
				}
			}

		} else if (e.getSource() == btnEdit) {
			readFileIndex = tableWord.getSelectedRow();
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + delimitField);
				int wordId = Integer.parseInt(arrId[0]);
				int meaningId = Integer.parseInt(arrId[1]);
				String oldMeaning = alWord.get(wordId).getMeaning(meaningId);
				tfWord.setEnabled(true);
				tfWord.setText(alWord.get(wordId).getName());
				tfPOSSym.setEnabled(true);
				if (modelWord.getValueAt(readFileIndex, 1) != null) {
					tfPOSSym.setText(alWord.get(wordId).getPOS());
				} else {
					tfPOSSym.setText("");
				}
				taMeaning.setText(oldMeaning);
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				taMeaning.setBorder(
						BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", tfWord, "Part of Speech", tfPOSSym, "Meaning:", spTAMeaning };
				int option = JOptionPane.showConfirmDialog(null, addingWords, "Edit a word",
						JOptionPane.OK_CANCEL_OPTION);
				String newWord = tfWord.getText();
				String newPOS= tfPOSSym.getText();
				String newMeaning = taMeaning.getText();

				if (option == 0) {
					if (newWord.length() == 0 || newMeaning.length() == 0) {
						JOptionPane.showMessageDialog(null, "You did not input word/meaning!", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else if (newMeaning.indexOf("\n") != -1) {
						JOptionPane.showMessageDialog(null,
								"Please edit one meaning at a time (i.e. do not use 'enter')", "Vocabulary Builder",
								JOptionPane.ERROR_MESSAGE);
					} else {
						alWord.get(wordId).setName(newWord);
						alWord.get(wordId).setPOS(newPOS);
						alWord.get(wordId).setMeaning(newMeaning, meaningId);
						wordArrayToTable(alWord);
						wordArrayToFile(alWord);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please select a row to edit", "Vocabulary Builder",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == btnDelete) {
			readFileIndex = tableWord.getSelectedRow();
			if (readFileIndex >= 0 && modelWord.getValueAt(readFileIndex, 0) != null) {
				Object[] deleteOptions = { "Delete Meaning", "Delete Word" };
				int option = JOptionPane.showOptionDialog(null, "Delete meaning or word?", "Vocabulary Builder",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, deleteOptions, null);
				String[] arrId = String.valueOf(modelWord.getValueAt(readFileIndex, 0)).split("\\" + delimitField);
				int wordId = Integer.parseInt(arrId[0]);

				if (option == 1) { // delete word
					if (alRevise.remove(alWord.get(wordId))) {
						if (wordReviewing.equals(alWord.get(wordId))) {
							setNewWord();
						}
					}
					alWord.get(wordId).delete();
					wordArrayToTable(alWord);
					wordArrayToFile(alWord);
				} else if (option == 0) {
					int meaningId = Integer.parseInt(arrId[1]);
					if (meaningId == 0 && alWord.get(wordId).getNumMeaning() == 1) {
						JOptionPane.showMessageDialog(null, "Every word must have at least 1 meaning.",
								"Vocabulary Builder", JOptionPane.ERROR_MESSAGE);
					} else {
						alWord.get(wordId).removeMeaning(meaningId);
						clearTableModel(modelWord);
						wordArrayToTable(alWord);
						wordArrayToFile(alWord);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please select a row to delete", "Vocabulary Builder",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == btnSearch) {
			taSearchResults.setText("");
			meaningSearch = new ArrayList<String>();
			btnAddtoList.setEnabled(true);
			wordSearch = tfWordSearch.getText();
			meaningSearch = searchWord(wordSearch, "oxford");
			if (meaningSearch != null) {
				for (int i = 0; i < meaningSearch.size(); i++) {
					taSearchResults.append(i + 1 + ". " + meaningSearch.get(i) + "\n");
				}
			}
		} else if (e.getSource() == btnAddtoList) {
			if (taSearchResults.getText().equals("") || wordSearch.length() == 0) {
				JOptionPane.showMessageDialog(null, "Word cannot be added, since word/and or meaning is empty",
						"Vocabulary Builder", JOptionPane.ERROR_MESSAGE);
			} else {
				Word newWord = new Word(id, wordSearch, "", meaningSearch, 0, LocalDate.now(), LocalDate.MIN,
						LocalDate.now().plusDays(1));
				id++;
				alWord.add(newWord);
				wordArrayToTable(alWord);
				wordArrayToFile(alWord);
				JOptionPane.showMessageDialog(null, wordSearch + " successfully added to list!", "Vocabulary Builder",
						JOptionPane.INFORMATION_MESSAGE);
				btnAddtoList.setEnabled(false);
			}
		} else if (e.getSource() == btnReviewAnswer) {
			btnReviewAnswer.setVisible(false);
			btnRemember.setEnabled(true);
			btnDontRemember.setEnabled(true);
		} else if (e.getSource() == btnRemember) {
			if (alRevise.remove(wordReviewing)) {
				increaseWordLevel(wordReviewing.getId());
			}
			lblNoOfWordsRemaining.setText(String.valueOf(alRevise.size()));
			setNewWord();
		} else if (e.getSource() == btnDontRemember) {
			wordReviewing.dontRemember();
			setNewWord();
		} else if (e.getSource() == btnPickWord || e.getSource() == btnBackDict2) {
			drawPickAWordListPanel();
		} else if (e.getSource() == btnSAT) {
			noOfExtWordListWords = 0;
			panelAddWord.removeAll();
			panelAddWord.revalidate();
			panelAddWord.repaint();
			panelAddWord.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 10));
			JLabel lblSAT = new JLabel("SAT Word List");
			lblSAT.setFont(fontTitle);
			lblSAT.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnAddSAT = new JButton("Add words to list");
			btnAddSAT.addActionListener(this);
			btnAddSAT.setBackground(Color.GREEN);
			alSAT = getSATList(fileSAT);
			modelExtWordList = new DefaultTableModel(0, greColTitle.length);
			modelExtWordList.setColumnIdentifiers(greColTitle);
			modelExtWordList.addTableModelListener(this);
			tableExtList = new JTable(modelExtWordList) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int column) {
					switch (column) {
					case 3:
						return true;
					default:
						return false;
					}
				};

				public Class getColumnClass(int column) {
					switch (column) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					case 2:
						return String.class;
					default:
						return Boolean.class;
					}
				}
			};
			tableExtList.getColumnModel().getColumn(0).setMinWidth(100);
			tableExtList.getColumnModel().getColumn(1).setMinWidth(100);
			tableExtList.getColumnModel().getColumn(2).setMinWidth(500);
			tableExtList.getColumnModel().getColumn(3).setMinWidth(100);
			JTableHeader header = tableExtList.getTableHeader();
			header.setPreferredSize(new Dimension(100, 40));
			for (int i = 0; i < alSAT.size(); i++) {
				modelExtWordList.addRow(new Object[] { alSAT.get(i).getName(), alSAT.get(i).getPOS(),
						alSAT.get(i).getMeaning(0), false });
			}
			tableExtList.setPreferredScrollableViewportSize(new Dimension(800, 550));
			tableExtList.setFillsViewportHeight(true);
			tableExtList.setPreferredSize(null);
			btnBackDict2 = new JButton("Back");
			btnBackDict2.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnBackDict2.addActionListener(this);
			btnBackDict2.setBackground(Color.white);

			panelAddWord.add(lblSAT);
			panelAddWord.add(btnAddSAT);
			panelAddWord.add(new JScrollPane(tableExtList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			panelAddWord.add(btnBackDict2);
		} else if (e.getSource() == btnTOEFL) {
			noOfExtWordListWords = 0;
			panelAddWord.removeAll();
			panelAddWord.revalidate();
			panelAddWord.repaint();
			panelAddWord.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 10));
			JLabel lblTOEFL = new JLabel("TOEFL Word List");
			lblTOEFL.setFont(fontTitle);
			lblTOEFL.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnAddTOEFL = new JButton("Add words to list");
			btnAddTOEFL.addActionListener(this);
			btnAddTOEFL.setBackground(Color.GREEN);
			alTOEFL = getTOEFLList(fileTOEFL);
			modelExtWordList = new DefaultTableModel(0, toeflColTitle.length);
			modelExtWordList.setColumnIdentifiers(toeflColTitle);
			modelExtWordList.addTableModelListener(this);
			tableExtList = new JTable(modelExtWordList) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int column) {
					switch (column) {
					case 2:
						return true;
					default:
						return false;
					}
				};

				public Class getColumnClass(int column) {
					switch (column) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					default:
						return Boolean.class;
					}
				}
			};
			tableExtList.getColumnModel().getColumn(0).setMinWidth(100);
			tableExtList.getColumnModel().getColumn(1).setMinWidth(600);
			tableExtList.getColumnModel().getColumn(2).setMinWidth(100);
			for (int i = 0; i < alTOEFL.size(); i++) {
				modelExtWordList.addRow(new Object[] { alTOEFL.get(i).getName(), alTOEFL.get(i).getMeaning(0), false });
			}
			tableExtList.setPreferredScrollableViewportSize(new Dimension(800, 550));
			tableExtList.setFillsViewportHeight(true);
			tableExtList.setPreferredSize(null);
			btnBackDict2 = new JButton("Back");
			btnBackDict2.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnBackDict2.addActionListener(this);
			btnBackDict2.setBackground(Color.white);

			panelAddWord.add(lblTOEFL);
			panelAddWord.add(btnAddTOEFL);
			panelAddWord.add(new JScrollPane(tableExtList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			panelAddWord.add(btnBackDict2);
		} else if (e.getSource() == btnGRE) {
			noOfExtWordListWords = 0;
			panelAddWord.removeAll();
			panelAddWord.revalidate();
			panelAddWord.repaint();
			panelAddWord.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 10));
			JLabel lblGRE = new JLabel("GRE Word List");
			lblGRE.setFont(fontTitle);
			lblGRE.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnAddGRE = new JButton("Add words to list");
			btnAddGRE.addActionListener(this);
			btnAddGRE.setBackground(Color.GREEN);
			alGRE = getGREList(fileGRE);
			modelExtWordList = new DefaultTableModel(0, greColTitle.length);
			modelExtWordList.setColumnIdentifiers(greColTitle);
			modelExtWordList.addTableModelListener(this);
			tableExtList = new JTable(modelExtWordList) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int column) {
					switch (column) {
					case 3:
						return true;
					default:
						return false;
					}
				};

				public Class getColumnClass(int column) {
					switch (column) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					case 2:
						return String.class;
					default:
						return Boolean.class;
					}
				}
			};
			tableExtList.getColumnModel().getColumn(0).setMinWidth(100);
			tableExtList.getColumnModel().getColumn(1).setMinWidth(100);
			tableExtList.getColumnModel().getColumn(2).setMinWidth(500);
			tableExtList.getColumnModel().getColumn(3).setMinWidth(100);
			JTableHeader header = tableExtList.getTableHeader();
			header.setPreferredSize(new Dimension(100, 40));
			for (int i = 0; i < alGRE.size(); i++) {
				modelExtWordList.addRow(new Object[] { alGRE.get(i).getName(), alGRE.get(i).getPOS(),
						alGRE.get(i).getMeaning(0), false });
			}
			tableExtList.setPreferredScrollableViewportSize(new Dimension(800, 550));
			tableExtList.setFillsViewportHeight(true);
			tableExtList.setPreferredSize(null);
			btnBackDict2 = new JButton("Back");
			btnBackDict2.setAlignmentX(Component.CENTER_ALIGNMENT);
			btnBackDict2.addActionListener(this);
			btnBackDict2.setBackground(Color.white);

			panelAddWord.add(lblGRE);
			panelAddWord.add(btnAddGRE);
			panelAddWord.add(new JScrollPane(tableExtList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
			panelAddWord.add(btnBackDict2);
		} else if (e.getSource() == btnAddSAT) {
			int option = 0;
			if (noOfExtWordListWords > 5) {
				option = JOptionPane.showConfirmDialog(null,
						"You are adding more than 5 words at a time. Are you sure you want to proceed?",
						"Vocabulary Builder", JOptionPane.YES_NO_OPTION);
			}
			if (option == 0 || noOfExtWordListWords <= 5) {
				for (int i = 0; i < alSAT.size(); i++) {
					if ((boolean) modelExtWordList.getValueAt(i, 3)) {
						ArrayList<String> meaningList = new ArrayList<String>();
						meaningList.add(modelExtWordList.getValueAt(i, 2).toString());
						Word newWord = new Word(id, modelExtWordList.getValueAt(i, 0).toString(),
								modelExtWordList.getValueAt(i, 1).toString(), meaningList, 0, LocalDate.now(),
								LocalDate.MIN, LocalDate.now().plusDays(1));
						id++;
						alWord.add(newWord);
					}
				}
				wordArrayToTable(alWord);
				wordArrayToFile(alWord);
				JOptionPane.showMessageDialog(null, "Words successfully added to list!", "Vocabulary Builder",
						JOptionPane.INFORMATION_MESSAGE);
				for (int i = 0; i < alSAT.size(); i++) {
					modelExtWordList.setValueAt(false, i, 3);
				}
			}
		} else if (e.getSource() == btnAddTOEFL) {
			int option = 0;
			if (noOfExtWordListWords > 5) {
				option = JOptionPane.showConfirmDialog(null,
						"You are adding more than 5 words at a time. Are you sure you want to proceed?",
						"Vocabulary Builder", JOptionPane.YES_NO_OPTION);
			}
			if (noOfExtWordListWords <= 5 || option == 0) {
				for (int i = 0; i < alTOEFL.size(); i++) {
					if ((boolean) modelExtWordList.getValueAt(i, 2)) {
						ArrayList<String> meaningList = new ArrayList<String>();
						meaningList.add(modelExtWordList.getValueAt(i, 1).toString());
						Word newWord = new Word(id, modelExtWordList.getValueAt(i, 0).toString(), "", meaningList, 0,
								LocalDate.now(), LocalDate.MIN, LocalDate.now().plusDays(1));
						id++;
						alWord.add(newWord);
					}
				}
				wordArrayToTable(alWord);
				wordArrayToFile(alWord);
				JOptionPane.showMessageDialog(null, "Words successfully added to list!", "Vocabulary Builder",
						JOptionPane.INFORMATION_MESSAGE);
				for (int i = 0; i < alTOEFL.size(); i++) {
					modelExtWordList.setValueAt(false, i, 2);
				}
			}
		} else if (e.getSource() == btnAddGRE) {
			int option = 0;
			if (noOfExtWordListWords > 5) {
				option = JOptionPane.showConfirmDialog(null,
						"You are adding more than 5 words at a time. Are you sure you want to proceed?",
						"Vocabulary Builder", JOptionPane.YES_NO_OPTION);
			}
			if (noOfExtWordListWords <= 5 || option == 0) {
				for (int i = 0; i < alGRE.size(); i++) {
					if ((boolean) modelExtWordList.getValueAt(i, 3)) {
						ArrayList<String> meaningList = new ArrayList<String>();
						meaningList.add(modelExtWordList.getValueAt(i, 2).toString());
						Word newWord = new Word(id, modelExtWordList.getValueAt(i, 0).toString(),
								modelExtWordList.getValueAt(i, 1).toString(), meaningList, 0, LocalDate.now(),
								LocalDate.MIN, LocalDate.now().plusDays(1));
						id++;
						alWord.add(newWord);
					}
				}
				wordArrayToTable(alWord);
				wordArrayToFile(alWord);
				JOptionPane.showMessageDialog(null, "Words successfully added to list!", "Vocabulary Builder",
						JOptionPane.INFORMATION_MESSAGE);
				for (int i = 0; i < alGRE.size(); i++) {
					modelExtWordList.setValueAt(false, i, 3);
				}
			}
		} else if (e.getSource() == btnBackDict1) {
			panelAddWord.removeAll();
			panelAddWord.revalidate();
			panelAddWord.repaint();
			panelAddWord.setLayout(new FlowLayout());
			panelAddWord.add(lblTypeWord);
			panelAddWord.add(Box.createVerticalStrut(20));
			panelAddWord.add(tfWordSearch);
			panelAddWord.add(btnSearch);
			panelAddWord.add(btnAddtoList);
			panelAddWord.add(Box.createVerticalStrut(20));
			panelAddWord.add(taSearchResults);
			panelAddWord.add(lblOr);
			panelAddWord.add(btnPickWord);
		}
	}

	public void stateChanged(ChangeEvent arg0) {
		if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Review") && alRevise.size() > 0) {
			lblNoOfWordsRemaining.setText(String.valueOf(alRevise.size()));
			if (alRevise.size() == 1) {
				JOptionPane.showMessageDialog(null, "You have " + alRevise.size() + " word to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "You have " + alRevise.size() + " words to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			}

			if (alRevise.size() > 0) {
				wordReviewing = alRevise.get(reviseIndex);
				lblWordReview.setText(alRevise.get(reviseIndex).getName());
				taAnswer.setText(alRevise.get(reviseIndex).getMeaning());
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Stats")) {
			taStat.setText("");
			taStat.append("Total Number of Words: " + alWord.size() + "\n");
			int wordsPastWeek = 0;
			for (int i = 0; i < alWord.size(); i++) {
				long diff = Duration
						.between(LocalDate.now().atStartOfDay(), alWord.get(i).getDateAdded().atStartOfDay()).toDays();
				if (diff >= -7) {
					wordsPastWeek++;
				}
			}
			taStat.append("Number of new words in past week: " + wordsPastWeek + "\n");
			for (int i = 0; i < noOfWordsInLevel.length; i++) {
				if (i == 10) {
					taStat.append("Number of words mastered: " + noOfWordsInLevel[i] + "\n");
				} else {
					taStat.append("Number of words in level " + i + ": " + noOfWordsInLevel[i] + "\n");
				}
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals("Word List")) {
			wordArrayToTable(alWord);
		}
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
					if (meaningData.size() == 1 && meaningData.get(0).length() == 0) { // no meanings
						meaningData = new ArrayList<String>();
					}
					String[] addDateArray = data[4].split(delimitDate);
					String[] reviseDateArray = data[6].split(delimitDate);
					String[] completeDateArray = data[5].split(delimitDate);
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
						alRevise.add(wordFromFile);
					}
					noOfWordsInLevel[wordFromFile.getLevel()]++;
					alWord.add(wordFromFile);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
			}
		}
	}

	public void wordArrayToTable(ArrayList<Word> aw) {
		modelWord.setRowCount(0);
		for (int i = 0; i < aw.size(); i++) {
			Word word = aw.get(i);
			if (word.getName() == "") {
				continue;
			}
			modelWord.addRow(new Object[] {word.getId() + delimitField + "0", word.getName(), word.getPOS(), word.getMeaning(0), 
					word.getLevel() >= 10 ? "Mastered" : word.getLevel() + delimitDate + (noOfWordsInLevel.length - 1),
							word.getDateAdded()});
			for (int j = 1; j < word.getNumMeaning(); j++) {
				modelWord.addRow(new Object[] {word.getId() + delimitField + j, null, null, word.getMeaning(j), null, null});
			}
		}
	}

	public void wordArrayToFile(ArrayList<Word> aw) {
		if (fileWord.delete()) {
			fileWord = new File("resources/words.txt");
		}
		try {
			out = new BufferedWriter(new FileWriter(fileWord, true));
			for (int i = 0; i < aw.size(); i++) {
				Word word = aw.get(i);
				if (word.getName() == "") {
					continue;
				}
				String meaningToFile = "";
				for (int mi = 0; mi < word.getNumMeaning(); mi++) {
					meaningToFile = meaningToFile.concat(word.getMeaning(mi));
					if (mi != word.getNumMeaning() - 1) {
						meaningToFile = meaningToFile.concat(delimitMean);
					}
				}
				out.write(word.getName() + delimitField);
				out.write(word.getPOS() + delimitField);
				out.write(meaningToFile + delimitField);
				out.write(word.getLevel() + delimitField);
				out.write(word.getDateAdded().getDayOfMonth() + delimitDate + word.getDateAdded().getMonthValue()
						+ delimitDate + word.getDateAdded().getYear() + delimitField);
				out.write(
						word.getDateCompleted().getDayOfMonth() + delimitDate + word.getDateCompleted().getMonthValue()
								+ delimitDate + word.getDateCompleted().getYear() + delimitField);
				out.write(word.getDateRevise().getDayOfMonth() + delimitDate + word.getDateRevise().getMonthValue()
						+ delimitDate + word.getDateRevise().getYear());
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
				// Request authentication
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
					// Do not put values in subsenses into the meaning
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
			}
			// Discontinued: a Chinese dictionary
			/*
			 * else if (dict.equals("dict cn")) { url = new URL("http://dict.cn/" + word);
			 * BufferedReader in = new BufferedReader(new
			 * InputStreamReader(url.openStream()));
			 * 
			 * String output; // TODO Search love while ((output = in.readLine()) != null) {
			 * if (output.contains("strong")) { System.out.println(output);
			 * meanings.add(output.trim().replaceAll("^\"|\"$", "").replaceAll("<strong>",
			 * "") .replaceAll("</strong>", "")); }
			 * 
			 * }
			 * 
			 * in.close(); return meanings; }
			 */ else {
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
		for (int i = 0; i < alWord.size(); i++) {
			if (alWord.get(i).getId() == wid) {
				alWord.get(i).incLevel();
				alWord.get(i).setNewRevDate();
				wordArrayToTable(alWord);
				wordArrayToFile(alWord);
			}

		}
	}

	public void setNewWord() {
		btnReviewAnswer.setVisible(true);
		btnRemember.setEnabled(false);
		btnDontRemember.setEnabled(false);
		if (alRevise.size() > 0) {
			if (reviseIndex >= alRevise.size() - 1) {
				reviseIndex = 0;
			} else {
				reviseIndex++;
			}
			wordReviewing = alRevise.get(reviseIndex);
			lblWordReview.setText(alRevise.get(reviseIndex).getName());
			taAnswer.setText(alRevise.get(reviseIndex).getMeaning());
		} else {
			btnReviewAnswer.setEnabled(false);
			lblWordReview.setText("");
			JOptionPane.showMessageDialog(null, "Congratulations! You have completed this review session!",
					"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	public ArrayList<Word> getSATList(File f) {
		ArrayList<Word> listSAT = new ArrayList<Word>();
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				line = in.readLine();
				while (line != null) {
					String[] arrLine = line.split("\\" + delimitField);
					Word wordSAT = new Word(arrLine[0], arrLine[1], arrLine[2]);
					listSAT.add(wordSAT);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("IOException: " + e.getMessage());
			}
		}
		return listSAT;
	}

	public ArrayList<Word> getTOEFLList(File f) {
		ArrayList<Word> listTOEFL = new ArrayList<Word>();
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				line = in.readLine();
				while (line != null) {
					String[] arrLine = line.split(": ");
					Word wordTOEFL = new Word(arrLine[0], arrLine[1]);
					listTOEFL.add(wordTOEFL);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("IOException: " + e.getMessage());
			}
		}
		return listTOEFL;
	}

	public ArrayList<Word> getGREList(File f) {
		ArrayList<Word> listGRE = new ArrayList<Word>();
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				line = in.readLine();
				while (line != null) {
					String[] arrLine = line.split("\\" + delimitField);
					Word wordGRE = new Word(arrLine[0], arrLine[1], arrLine[2]);
					listGRE.add(wordGRE);
					line = in.readLine();
				}
				in.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("IOException: " + e.getMessage());
			}
		}
		return listGRE;
	}

	public void drawPickAWordListPanel() {
		// pick a word list panel
		panelAddWord.removeAll();
		panelAddWord.revalidate();
		panelAddWord.repaint();
		panelAddWord.setLayout(new BoxLayout(panelAddWord, BoxLayout.PAGE_AXIS));
		JLabel lblChooseList = new JLabel("Choose a list:");
		lblChooseList.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblChooseList.setFont(fontTitle);
		btnSAT = new JButton("SAT");
		btnSAT.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSAT.setFont(fontLargeBtn);
		btnSAT.setBackground(Color.GRAY);
		btnSAT.addActionListener(this);
		btnTOEFL = new JButton("TOEFL");
		btnTOEFL.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnTOEFL.setFont(fontLargeBtn);
		btnTOEFL.addActionListener(this);
		btnTOEFL.setBackground(Color.GRAY);
		btnGRE = new JButton("GRE");
		btnGRE.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnGRE.setFont(fontLargeBtn);
		btnGRE.addActionListener(this);
		btnGRE.setBackground(Color.GRAY);
		btnBackDict1 = new JButton("Back");
		btnBackDict1.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnBackDict1.addActionListener(this);
		btnBackDict1.setBackground(Color.white);

		panelAddWord.add(lblChooseList);
		panelAddWord.add(Box.createVerticalStrut(100));
		panelAddWord.add(btnSAT);
		panelAddWord.add(Box.createVerticalStrut(100));
		panelAddWord.add(btnTOEFL);
		panelAddWord.add(Box.createVerticalStrut(100));
		panelAddWord.add(btnGRE);
		panelAddWord.add(Box.createVerticalStrut(20));
		panelAddWord.add(btnBackDict1);
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE
				&& modelExtWordList.getValueAt(e.getFirstRow(), e.getColumn()).toString().equals("true")) {
			noOfExtWordListWords++;
		} else if (e.getType() == TableModelEvent.UPDATE
				&& modelExtWordList.getValueAt(e.getFirstRow(), e.getColumn()).toString().equals("false")) {
			noOfExtWordListWords--;
		}
	}
}