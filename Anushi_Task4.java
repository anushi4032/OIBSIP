import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Anushi_Task4 extends JFrame {

   
    static class User {
        String username;
        String password;
        String displayName;
        User(String username, String password, String displayName) {
            this.username = username;
            this.password = password;
            this.displayName = displayName;
        }
    }

    static class Question {
        String text;
        String[] options;
        int correctIndex; // 0..3
        Question(String text, String[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

  
    static final String CARD_LOGIN = "LOGIN";
    static final String CARD_PROFILE = "PROFILE";
    static final String CARD_EXAM = "EXAM";
    static final String CARD_RESULT = "RESULT";

    static final int EXAM_DURATION_SECONDS = 30 * 60; // 30 minutes

    Map<String, User> users = new HashMap<>();
    User currentUser;

    List<Question> questions = new ArrayList<>();
    int[] selectedAnswers;      // -1 = unanswered
    int currentQuestionIndex;
    int remainingSeconds;
    javax.swing.Timer examTimer;
    long examStartMillis;
    long timeTakenSeconds;
    boolean examInProgress = false;

    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    LoginPanel loginPanel;
    ProfilePanel profilePanel;
    ExamPanel examPanel;
    ResultPanel resultPanel;

    public Anushi_Task4() {
        super("Simple MCQ Exam System");
        seedUsers();
        seedQuestions();

        loginPanel = new LoginPanel(this);
        profilePanel = new ProfilePanel(this);
        examPanel = new ExamPanel(this);
        resultPanel = new ResultPanel(this);

        mainPanel.add(loginPanel, CARD_LOGIN);
        mainPanel.add(profilePanel, CARD_PROFILE);
        mainPanel.add(examPanel, CARD_EXAM);
        mainPanel.add(resultPanel, CARD_RESULT);

        setContentPane(mainPanel);
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        showCard(CARD_LOGIN);
    }

    private void seedUsers() {
        // username / password / display name
        users.put("student", new User("student", "pass123", "Student"));
        users.put("alice", new User("alice", "alice123", "Alice"));
    }

    private void seedQuestions() {
        questions.add(new Question(
                "What does GUI stand for?",
                new String[]{"Graphical User Interface", "General Use Instruction",
                        "Global User Identity", "Graphic Utility Input"},
                0));
        questions.add(new Question(
                "Which package provides Swing components in Java?",
                new String[]{"java.util", "java.awt", "javax.swing", "java.io"},
                2));
        questions.add(new Question(
                "Which class is used for a countdown timer in Swing?",
                new String[]{"java.util.Date", "javax.swing.Timer", "Thread", "Calendar"},
                1));
        questions.add(new Question(
                "Which layout manager is commonly used to switch between screens?",
                new String[]{"BorderLayout", "GridLayout", "CardLayout", "FlowLayout"},
                2));
        questions.add(new Question(
                "Which component group ensures only one radio button is selected at a time?",
                new String[]{"ButtonGroup", "JPanel", "JList", "ItemGroup"},
                0));
    }

    void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    /** Called after a successful login. */
    void onLoginSuccess(User user) {
        currentUser = user;
        profilePanel.loadUser(user);
        showCard(CARD_PROFILE);
    }

    /** Called when the user finishes updating their profile and wants to start the exam. */
    void startExam() {
        selectedAnswers = new int[questions.size()];
        Arrays.fill(selectedAnswers, -1);
        currentQuestionIndex = 0;
        remainingSeconds = EXAM_DURATION_SECONDS;
        examStartMillis = System.currentTimeMillis();
        examInProgress = true;

        examPanel.showQuestion(0);
        examPanel.updateTimerLabel();

        if (examTimer != null) {
            examTimer.stop();
        }
        examTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            examPanel.updateTimerLabel();
            if (remainingSeconds <= 0) {
                examTimer.stop();
                finishExam(true);
            }
        });
        examTimer.start();

        showCard(CARD_EXAM);
    }
    void finishExam(boolean autoSubmitted) {
        if (examTimer != null) {
            examTimer.stop();
        }
        examInProgress = false;
        examPanel.saveCurrentSelection(); // make sure the last-viewed answer is captured
        timeTakenSeconds = (System.currentTimeMillis() - examStartMillis) / 1000L;

        resultPanel.showResult(autoSubmitted);
        showCard(CARD_RESULT);
    }

    /** Called from the result screen's Logout button. */
    void logout() {
        currentUser = null;
        loginPanel.clearFields();
        showCard(CARD_LOGIN);
    }

    private void handleWindowClose() {
        if (examInProgress) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "The exam is still in progress. Are you sure you want to quit?",
                    "Confirm Quit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
            // else: do nothing, stay open
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Anushi_Task4 app = new Anushi_Task4();
            app.setVisible(true);
        });
    }
}


class LoginPanel extends JPanel {
    private final Anushi_Task4 app;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    LoginPanel(Anushi_Task4 app) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("Exam Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginBtn, gbc);

        JLabel hint = new JLabel("<html><i>Try: student / pass123</i></html>");
        gbc.gridy = 4;
        add(hint, gbc);

        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        Anushi_Task4.User user = app.users.get(username);
        if (user != null && user.password.equals(password)) {
            attemptLoginSuccess(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptLoginSuccess(Anushi_Task4.User user) {
        app.onLoginSuccess(user);
    }

    void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}


class ProfilePanel extends JPanel {
    private final Anushi_Task4 app;
    private final JLabel welcomeLabel = new JLabel();
    private final JTextField displayNameField = new JTextField(15);
    private final JPasswordField newPasswordField = new JPasswordField(15);

    ProfilePanel(Anushi_Task4 app) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(welcomeLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Display Name:"), gbc);
        gbc.gridx = 1;
        add(displayNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        add(newPasswordField, gbc);

        JLabel note = new JLabel("<html><i>Leave password blank to keep it unchanged.</i></html>");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(note, gbc);

        JButton startBtn = new JButton("Save & Start Exam");
        gbc.gridy = 4;
        add(startBtn, gbc);

        startBtn.addActionListener(e -> saveAndStart());
    }

    void loadUser(Anushi_Task4.User user) {
        welcomeLabel.setText("Welcome, " + user.displayName + "!");
        displayNameField.setText(user.displayName);
        newPasswordField.setText("");
    }

    private void saveAndStart() {
        Anushi_Task4.User user = app.currentUser;
        String newDisplayName = displayNameField.getText().trim();
        if (!newDisplayName.isEmpty()) {
            user.displayName = newDisplayName;
        }
        String newPassword = new String(newPasswordField.getPassword());
        if (!newPassword.isEmpty()) {
            user.password = newPassword;
        }
        app.startExam();
    }
}


class ExamPanel extends JPanel {
    private final Anushi_Task4 app;

    private final JLabel timerLabel = new JLabel();
    private final JLabel questionNumberLabel = new JLabel();
    private final JTextArea questionTextArea = new JTextArea();
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup buttonGroup = new ButtonGroup();

    private final JButton prevBtn = new JButton("Previous");
    private final JButton nextBtn = new JButton("Next");
    private final JButton submitBtn = new JButton("Submit Exam");

    ExamPanel(Anushi_Task4 app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top bar: question number + timer
        JPanel topBar = new JPanel(new BorderLayout());
        questionNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerLabel.setForeground(new Color(180, 0, 0));
        topBar.add(questionNumberLabel, BorderLayout.WEST);
        topBar.add(timerLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // center: question + options
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        questionTextArea.setEditable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        questionTextArea.setOpaque(false);
        questionTextArea.setFocusable(false);
        centerPanel.add(questionTextArea);
        centerPanel.add(Box.createVerticalStrut(15));

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            buttonGroup.add(optionButtons[i]);
            centerPanel.add(optionButtons[i]);
            centerPanel.add(Box.createVerticalStrut(8));
        }

        add(centerPanel, BorderLayout.CENTER);

        // bottom: navigation + submit
        JPanel bottomBar = new JPanel(new FlowLayout());
        bottomBar.add(prevBtn);
        bottomBar.add(nextBtn);
        bottomBar.add(submitBtn);
        add(bottomBar, BorderLayout.SOUTH);

        prevBtn.addActionListener(e -> goToQuestion(app.currentQuestionIndex - 1));
        nextBtn.addActionListener(e -> goToQuestion(app.currentQuestionIndex + 1));
        submitBtn.addActionListener(e -> confirmSubmit());
    }

    /** Saves the currently-selected radio option into app.selectedAnswers[]. */
    void saveCurrentSelection() {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        if (app.selectedAnswers != null) {
            app.selectedAnswers[app.currentQuestionIndex] = selected;
        }
    }

    private void goToQuestion(int newIndex) {
        if (newIndex < 0 || newIndex >= app.questions.size()) {
            return; // out of range, ignore
        }
        saveCurrentSelection();
        showQuestion(newIndex);
    }

    void showQuestion(int index) {
        app.currentQuestionIndex = index;
        Anushi_Task4.Question q = app.questions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + app.questions.size());
        questionTextArea.setText(q.text);

        buttonGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText((char) ('A' + i) + ". " + q.options[i]);
        }
        int savedAnswer = app.selectedAnswers[index];
        if (savedAnswer >= 0) {
            optionButtons[savedAnswer].setSelected(true);
        }

        prevBtn.setEnabled(index > 0);
        nextBtn.setEnabled(index < app.questions.size() - 1);
    }

    void updateTimerLabel() {
        int minutes = Math.max(app.remainingSeconds, 0) / 60;
        int seconds = Math.max(app.remainingSeconds, 0) % 60;
        timerLabel.setText(String.format("Time Left: %02d:%02d", minutes, seconds));
    }

    private void confirmSubmit() {
        saveCurrentSelection();
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit the exam?",
                "Confirm Submit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            app.finishExam(false);
        }
    }
}


class ResultPanel extends JPanel {
    private final Anushi_Task4 app;
    private final JLabel summaryLabel = new JLabel();
    private final JTextArea breakdownArea = new JTextArea();

    ResultPanel(Anushi_Task4 app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(summaryLabel, BorderLayout.NORTH);

        breakdownArea.setEditable(false);
        breakdownArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(breakdownArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.add(logoutBtn);
        add(bottomBar, BorderLayout.SOUTH);

        logoutBtn.addActionListener(e -> app.logout());
    }

    void showResult(boolean autoSubmitted) {
        int total = app.questions.size();
        int score = 0;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < total; i++) {
            Anushi_Task4.Question q = app.questions.get(i);
            int userAnswer = app.selectedAnswers[i];
            boolean correct = userAnswer == q.correctIndex;
            if (correct) score++;

            String userAnswerText = (userAnswer >= 0)
                    ? (char) ('A' + userAnswer) + ". " + q.options[userAnswer]
                    : "(no answer)";
            String correctAnswerText = (char) ('A' + q.correctIndex) + ". " + q.options[q.correctIndex];

            sb.append("Q").append(i + 1).append(": ").append(q.text).append("\n");
            sb.append("   Your answer:    ").append(userAnswerText).append("\n");
            sb.append("   Correct answer: ").append(correctAnswerText).append("\n");
            sb.append("   Result: ").append(correct ? "CORRECT" : "INCORRECT").append("\n\n");
        }

        long minutes = app.timeTakenSeconds / 60;
        long seconds = app.timeTakenSeconds % 60;

        String autoNote = autoSubmitted ? "  (auto-submitted: time ran out)" : "";
        summaryLabel.setText(String.format(
                "Score: %d out of %d   |   Time Taken: %02d:%02d%s",
                score, total, minutes, seconds, autoNote));

        breakdownArea.setText(sb.toString());
        breakdownArea.setCaretPosition(0);
    }
}
