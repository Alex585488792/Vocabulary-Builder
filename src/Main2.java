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
public class Main2 extends JFrame implements ActionListener, ChangeListener{
	private static final long serialVersionUID = 1L;
	private JPanel wordPanel, statPanel, reviewPanel, dictPanel;
	private JButton btnAddWord, btnEditWord, btnDeleteWord, btnSearch,
			btnAddtoList, btnReviewAnswer, btnRemember, btnDontRemember;
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
	private ArrayList<Word> wordList, reviseWordList;
	private int[] noOfWordsInLevel = new int[6];

	public static void main(String[] args) {
		new Main2();
	}
	public Main2() {
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
	}
}
