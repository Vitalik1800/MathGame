package com.vs18.mathgame;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.vs18.mathgame.databinding.ActivityMainBinding;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int num1, num2, correctAnswer, score = 0, highScore = 0;
    private String operation;
    private CountDownTimer timer;
    private int timeLeft = 30;
    private int difficulty = 1;
    private final Random random = new Random();
    private final String[] operations = {"+", "-", "×", "÷"};
    ActivityMainBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = prefs.getInt("highScore", 0);
        updateScore();

        generateTask();
        startTimer();

        binding.checkButton.setOnClickListener(v -> checkAnswer());

        binding.answerInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer();
                return true;
            }
            return false;
        });

        binding.timerText.setOnLongClickListener(v -> {
            difficulty = difficulty % 3 + 1;
            String level = difficulty == 1 ? "Легкий" : difficulty == 2 ? "Середній" : "Важкий";
            binding.levelText.setText("Рівень: " + level);
            Toast.makeText(this, "Рівень змінено: " + level, Toast.LENGTH_LONG).show();
            generateTask();
            return true;
        });
    }

    @SuppressLint("SetTextI18n")
    private void generateTask() {
        int maxNum = difficulty == 1 ? 10 : difficulty == 2 ? 50 : 100;

        num1 = random.nextInt(maxNum) + 1;
        num2 = random.nextInt(maxNum) + 1;
        operation = operations[random.nextInt(operations.length)];

        if (operation.equals("÷")) {
            num1 = num2 * (random.nextInt(maxNum / num2) + 1);
        }

        correctAnswer = calculateAnswer();

        binding.taskText.setText(num1 + " " + operation + " " + num2 + " = ?");
        binding.answerInput.setText("");
        binding.answerInput.requestFocus();
        binding.resultText.setText("");
    }

    private int calculateAnswer() {
        return switch (operation) {
            case "+" -> num1 + num2;
            case "-" -> num1 - num2;
            case "×" -> num1 * num2;
            case "÷" -> num1 / num2;
            default -> 0;
        };
    }

    @SuppressLint("SetTextI18n")
    private void checkAnswer() {
        String input = binding.answerInput.getText().toString().trim();
        if (input.isEmpty()) {
            binding.resultText.setText("Введіть відповідь!");
            binding.resultText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return;
        }

        int userAnswer = Integer.parseInt(input);

        if (userAnswer == correctAnswer) {
            score++;
            binding.resultText.setText("Правильно! ✅");
            binding.resultText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            binding.resultText.setText("Ні, правильна: " + correctAnswer + " ❌");
            binding.resultText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }

        updateScore();

        if (timeLeft > 0) {
            generateTask();
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                timeLeft = (int) (l / 1000);
                binding.timerText.setText("Час: " + timeLeft);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                binding.timerText.setText("Час вичерпано!");
                binding.answerInput.setEnabled(false);
                binding.checkButton.setEnabled(false);
                binding.resultText.setText("Гра закінчена! Ваш рахунок: " + score);

                if (score > highScore) {
                    highScore = score;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    prefs.edit().putInt("highScore", highScore).apply();
                    binding.resultText.append("\n🎉 Новий рекорд!");
                }

                updateScore();

                binding.checkButton.setText("Нова гра");
                binding.checkButton.setEnabled(true);
                binding.checkButton.setOnClickListener(v -> restartGame());
            }
        }.start();
    }

    private void restartGame() {
        score = 0;
        timeLeft = 30;
        updateScore();
        binding.answerInput.setEnabled(true);
        binding.checkButton.setText("Перевірити");
        binding.checkButton.setOnClickListener(v -> checkAnswer());
        binding.resultText.setText("");
        generateTask();
        startTimer();
    }

    @SuppressLint("SetTextI18n")
    private void updateScore() {
        binding.scoreText.setText("Бали: " + score + " | Рекорд: " + highScore);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}