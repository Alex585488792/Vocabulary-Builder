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
import java.text.SimpleDateFormat;
import java.awt.event.*;
import java.awt.*;

/**
 * Edit word doesn't work with replaceAll Different meanings cannot be separated
 * into lines
 * test
 * 
 * @author Alex
 *
 */
public class Main extends JFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 1L;
	private JPanel wordPanel, statPanel, reviewPanel, dictPanel;
	private JButton btnAddWord, btnEditWord, btnDeleteWord, btnSearch,
			btnAddtoList, btnReviewAnswer, btnRemember, btnDontRemember;
	private DefaultTableModel modelWord;
	public DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	private BufferedWriter out;
	private File wordFile = new File("words.txt"), statFile = new File(
			"stats.txt");
	private int readFileIndex = 0, noOfWords = 0, wordsToBeRevised = 0, wordsToBeRevised2 = 0, rwlindex = 0, wordListIndex = 0;
	private Object value, valueMeaning;
	private JTable wordTable;
	private JTextField wordTF, phoneticSymTF, wordSearchTF;
	private JTextArea meaningTA, searchResultsTA, answerTA, statTA;
	private JLabel wordReview, lblNoOfWordsRemaining;
	private JTabbedPane tabbedPane;
	private JScrollPane meaningTAScroll;
	private Word[] reviseWordList = new Word[500];
	private int[] noOfWordsInLevel = new int[6];

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
		String[] wordColumnTitle = { "Word", "<html>Phonetic<br> Symbol",
				"Meaning", "<html>Level of <br>completion", "Date Added"};
		modelWord = new DefaultTableModel(1000, wordColumnTitle.length);
		modelWord.setColumnIdentifiers(wordColumnTitle);
		wordTable = new JTable(modelWord) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		wordTable.getColumnModel().getColumn(0).setMinWidth(100);
		wordTable.getColumnModel().getColumn(1).setMinWidth(100);
		wordTable.getColumnModel().getColumn(2).setMinWidth(400);
		wordTable.getColumnModel().getColumn(3).setMinWidth(100);
		wordTable.getColumnModel().getColumn(4).setMinWidth(100);
		wordTable.setPreferredScrollableViewportSize(new Dimension(800, 600));
		wordTable.setFillsViewportHeight(true);
		wordTable.setPreferredSize(null);

		// read number of lines of file
		LineNumberReader lnr;
		String line;
		String[] data, meanData;
		if (wordFile.exists()) {
			try {
				lnr = new LineNumberReader(new FileReader(wordFile));
				try {
					lnr.skip(Long.MAX_VALUE);
					noOfWords = lnr.getLineNumber();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					lnr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// read from file
			try {
				BufferedReader in = new BufferedReader(new FileReader(wordFile));
				Word[] wordList = new Word[noOfWords];
				line = in.readLine();
				while (line != null) {
					data = line.split("::");
					meanData = data[2].split(";;");
					Word wordFromFile = new Word(data[0],data[1],meanData,Integer.parseInt(data[3]),"",data[4],Boolean.valueOf(data[5]));
					wordList[wordListIndex] = wordFromFile;
					modelWord.setValueAt(wordFromFile.name, readFileIndex, 0);
					modelWord.setValueAt(wordFromFile.phonetic, readFileIndex, 1);
					modelWord.setValueAt(wordFromFile.lv, readFileIndex, 3);
					if ((wordFromFile.lv) == 0) {
						noOfWordsInLevel[0]++;
					} else if (wordFromFile.lv == 1) {
						noOfWordsInLevel[1]++;
					} else if (wordFromFile.lv == 2) {
						noOfWordsInLevel[2]++;
					} else if (wordFromFile.lv == 3) {
						noOfWordsInLevel[3]++;
					} else if (wordFromFile.lv == 4) {
						noOfWordsInLevel[4]++;
					} else if (wordFromFile.lv == 5) {
						noOfWordsInLevel[5]++;
					}
					modelWord.setValueAt(data[4], readFileIndex, 4);
					if (Boolean.valueOf(data[5].toString())) {
						wordsToBeRevised++;
						reviseWordList[rwlindex] = wordFromFile;
						rwlindex++;
					}
					// meaning is inserted into the table last, because it changes the row index
					for (int i = 0; i < meanData.length; i++) {
						modelWord.setValueAt(wordFromFile.meaning[i], readFileIndex, 2);
						readFileIndex++;
					}
					wordListIndex++;
					line = in.readLine();
				}
				in.close();
				rwlindex = 0;
				wordsToBeRevised2 = wordsToBeRevised;
			} catch (IOException e) {
				System.err.println("IOException: " + e.getMessage());
			}
		}

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
		try {
			BufferedReader in = new BufferedReader(new FileReader(statFile));
			line = in.readLine();
			while (line != null) {
				if (line.contains("Total")) {
					statTA.append(line + " " + noOfWords + "\n");
				}
				if (line.contains("Level")) {
					statTA.append(line + " " + noOfWordsInLevel[rwlindex]
							+ "\n");
					rwlindex++;
				}
				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {

		}
		rwlindex = 0;

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
		if (reviseWordList[0] != null) {
			wordReview.setText(reviseWordList[0].name);
			answerTA.setText(reviseWordList[0].meaning[0]);
		}
		
		wordPanel.add(btnAddWord);
		wordPanel.add(btnEditWord);
		wordPanel.add(btnDeleteWord);
		wordPanel.add(new JScrollPane(wordTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
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
			readFileIndex = 0;
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
			String word = wordTF.getText();
			String phoneticSymbol = phoneticSymTF.getText();
			String [] meaning = meaningTA.getText().split("\n");
			if (option == 0) {
				if (word.length() == 0 || meaning.length == 0) {
					JOptionPane.showMessageDialog(null,
							"You did not input word/meaning!",
							"Vocabulary Builder",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					noOfWords++;
					noOfWordsInLevel[0]++;
					do {
						value = modelWord.getValueAt(readFileIndex, 0);
						valueMeaning = modelWord.getValueAt(readFileIndex, 2);
						if (value == null && valueMeaning == null) {
							modelWord.setValueAt(word, readFileIndex, 0);
							modelWord.setValueAt(phoneticSymbol, readFileIndex, 1);
							modelWord.setValueAt(0, readFileIndex, 3);
							modelWord.setValueAt(df.format(new Date()), readFileIndex,
									4);
							for (int i = 0; i < meaning.length; i++) {
								modelWord.setValueAt(meaning[i], readFileIndex+i, 2);
							}
							wordsToBeRevised++;
						}
						readFileIndex++;
					} while (value != null || valueMeaning != null);
					try {
						out = new BufferedWriter(new FileWriter(wordFile, true));
						String meaningToFile = "";
						for (int i = 0; i < meaning.length; i++) {
							meaningToFile = meaningToFile.concat(meaning[i]);
							if (i != meaning.length - 1)meaningToFile = meaningToFile.concat(";;");
						}
						out.write(word + "::" + phoneticSymbol + "::" + meaningToFile
								+ "::" + 0 + "::" + df.format(new Date())
								+ "::" + true);
						out.newLine();
						out.close();
					} catch (IOException f) {
						JOptionPane.showMessageDialog(null, f.getMessage()
								+ "!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} else if (e.getSource() == btnEditWord) {
			readFileIndex = wordTable.getSelectedRow();
			if (readFileIndex >= 0) {
				String oldMeaning = modelWord.getValueAt(readFileIndex, 2).toString();
				wordTF.setEnabled(false);
				int wordIndex = readFileIndex;
				while (modelWord.getValueAt(wordIndex, 0) == null) {
					wordIndex--;
				}
				wordTF.setText(modelWord.getValueAt(wordIndex, 0).toString());
				if (modelWord.getValueAt(readFileIndex, 1) != null) {
					phoneticSymTF.setText(modelWord.getValueAt(readFileIndex, 1)
							.toString());
				} else {
					phoneticSymTF.setText("");
				}
				meaningTA.setText(modelWord.getValueAt(readFileIndex, 2).toString());
				Border border = BorderFactory.createLineBorder(Color.BLACK);
				meaningTA.setBorder(BorderFactory.createCompoundBorder(border,
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));
				Object[] addingWords = { "Word:", wordTF, "Phonetic symbol",
						phoneticSymTF, "Meaning:", meaningTA };
				int option = JOptionPane.showConfirmDialog(null, addingWords,
						"Edit a word", JOptionPane.OK_CANCEL_OPTION);
				String word = wordTF.getText();
				String phoneticSymbol = phoneticSymTF.getText();
				String meaning = meaningTA.getText();
				if (option == 0) {
					if (word.length() == 0 || meaning.length() == 0) {
						JOptionPane.showMessageDialog(null,
								"You did not input word/meaning!",
								"Vocabulary Builder",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						modelWord.setValueAt(word, readFileIndex, 0);
						modelWord.setValueAt(phoneticSymbol, readFileIndex, 1);
						modelWord.setValueAt(meaning, readFileIndex, 2);
						Path path = Paths.get("words.txt");
						Charset charset = StandardCharsets.UTF_8;

						// replacing strings in file
						String content = null;
						try {
							content = new String(Files.readAllBytes(path),
									charset);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						content = content.replaceAll(oldMeaning, meaning);
						try {
							Files.write(path, content.getBytes(charset));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		} else if (e.getSource() == btnDeleteWord) {
			readFileIndex = wordTable.getSelectedRow();
			File temp = null;
			try {
				temp = File.createTempFile("file", ".txt",
						wordFile.getParentFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if (readFileIndex >= 0) {
				// remove the word from table
				for (int i = readFileIndex; i < noOfWords; i++) {
					modelWord.setValueAt(modelWord.getValueAt(i + 1, 0), i, 0);
					modelWord.setValueAt(modelWord.getValueAt(i + 1, 1), i, 1);
					modelWord.setValueAt(modelWord.getValueAt(i + 1, 2), i, 2);
					modelWord.setValueAt(modelWord.getValueAt(i + 1, 3), i, 3);
					modelWord.setValueAt(modelWord.getValueAt(i + 1, 4), i, 4);
				}
				// remove the word from file
				String line = "null";
				try {
					int i = 0;
					BufferedReader in = new BufferedReader(new FileReader(
							wordFile));
					BufferedWriter out = new BufferedWriter(
							new FileWriter(temp));
					while (line != null) {
						line = in.readLine();
						if (readFileIndex != i) {
							if (line != null) {
								out.write(line);
								out.newLine();
							}
						}
						i++;
					}
					in.close();
					out.close();
				} catch (IOException f) {
					System.err.println("IOException: " + f.getMessage());
				}
				wordFile.delete();
				temp.renameTo(wordFile);
				noOfWords--;
			}

		} else if (e.getSource() == btnSearch) {
			searchResultsTA.setText("");
			btnAddtoList.setEnabled(true);
			String s = "";
			boolean meaningFound = false;
			s = wordSearchTF.getText();
			try {
				URL oracle = new URL("http://dict.cn/" + s);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						oracle.openStream()));
				String inputLine;
				Pattern patternPartOfSpeech = Pattern
						.compile("<li><span>(.*?)</span>");
				Pattern patternMeaning = Pattern
						.compile("<strong>(.*?)</strong");
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.contains("<li><span>")) {
						inputLine = inputLine.trim();
						Matcher matcher = patternPartOfSpeech
								.matcher(inputLine);
						while (matcher.find() && !meaningFound) {
							searchResultsTA.append(matcher.group(1) + " ");
						}
						matcher = patternMeaning.matcher(inputLine);
						while (matcher.find() && !meaningFound) {
							searchResultsTA.append(matcher.group(1));
							meaningFound = true;
						}
					}
				}
				meaningFound = false;
				in.close();
			} catch (MalformedURLException f) {
			} catch (IOException g) {
			}

		} else if (e.getSource() == btnAddtoList) {
			btnAddtoList.setEnabled(false);
			noOfWords++;
			do {
				value = modelWord.getValueAt(readFileIndex, 0);
				if (value == null) {
					modelWord.setValueAt(wordSearchTF.getText(), readFileIndex, 0);
					modelWord.setValueAt("", readFileIndex, 1);
					modelWord.setValueAt(searchResultsTA.getText(), readFileIndex, 2);
					modelWord.setValueAt(df.format(new Date()), readFileIndex, 4);
				}
				if (value != null) {
					readFileIndex++;
				}
			} while (value != null);
			try {
				out = new BufferedWriter(new FileWriter(wordFile, true));
				out.write(modelWord.getValueAt(readFileIndex, 0) + "::" + modelWord.getValueAt(readFileIndex, 1) + "::"
						+ modelWord.getValueAt(readFileIndex, 2) + "::" + 0 + "::"
						+ modelWord.getValueAt(readFileIndex, 4) + "::" + true);
				out.newLine();
				out.close();
			} catch (IOException f) {
				JOptionPane.showMessageDialog(null, f.getMessage() + "!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == btnReviewAnswer) {
			btnReviewAnswer.setVisible(false);
			btnRemember.setEnabled(true);
			btnDontRemember.setEnabled(true);
		} else if (e.getSource() == btnRemember) {
			btnReviewAnswer.setVisible(true);
			btnRemember.setEnabled(false);
			btnDontRemember.setEnabled(false);
			wordsToBeRevised--;
			lblNoOfWordsRemaining.setText(String.valueOf(wordsToBeRevised));
			for (int i = 0; i < noOfWords; i++) {
				// raise level set rev to false
				if (modelWord.getValueAt(i, 0) == reviseWordList[rwlindex].name) {
					System.out.println(modelWord.getValueAt(i, 3));
					modelWord.setValueAt(Integer.parseInt(modelWord.getValueAt(
							rwlindex, 3).toString()) + 1, i, 3);
					System.out.println(modelWord.getValueAt(i, 3));
					modelWord.setValueAt(false, i, 5);
				}
			}
			tableToFile();
			reviseWordList[rwlindex].name = "";
			reviseWordList[rwlindex].meaning[0] = "";
			rwlindex++;
			wordReview.setText(reviseWordList[rwlindex].name);
			answerTA.setText(reviseWordList[rwlindex].meaning[0]);
			if (wordsToBeRevised == 0) {
				wordReview.setText("");
				btnReviewAnswer.setEnabled(false);
				JOptionPane.showMessageDialog(null,
						"You have completed this review session.",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (e.getSource() == btnDontRemember) {
			btnReviewAnswer.setVisible(true);
			btnRemember.setEnabled(false);
			btnDontRemember.setEnabled(false);
			rwlindex++;
			while (reviseWordList[rwlindex].name == null) {
				rwlindex++;
				if (rwlindex > wordsToBeRevised2) {
					rwlindex = 0;
				}
			}
			wordReview.setText(reviseWordList[rwlindex].name);
			answerTA.setText(reviseWordList[rwlindex].meaning[0]);
		}
	}

	public void stateChanged(ChangeEvent arg0) {
		if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(
				"Review")
				&& wordsToBeRevised > 0) {
			lblNoOfWordsRemaining.setText(String.valueOf(wordsToBeRevised));
			if (wordsToBeRevised == 1) {
				JOptionPane.showMessageDialog(null, "You have "
						+ wordsToBeRevised + " word to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "You have "
						+ wordsToBeRevised + " words to be revised!",
						"Vocabulary Builder", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(
				"Stats")) {

		}
	}

	public void tableToFile() {
		File temp = null;
		try {
			temp = File
					.createTempFile("file", ".txt", wordFile.getParentFile());
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			int i = 0;
			while (wordTable.getValueAt(i, 0) != null) {
				out.write(wordTable.getValueAt(i, 0) + "::"
						+ wordTable.getValueAt(i, 1) + "::"
						+ wordTable.getValueAt(i, 2) + "::"
						+ wordTable.getValueAt(i, 3) + "::"
						+ wordTable.getValueAt(i, 4) + "::"
						+ wordTable.getValueAt(i, 5));
				i++;
				out.newLine();
			}
			out.close();
			wordFile.delete();
			temp.renameTo(wordFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}