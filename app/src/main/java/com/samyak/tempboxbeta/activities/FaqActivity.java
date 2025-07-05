package com.samyak.tempboxbeta.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.samyak.tempboxbeta.R;

public class FaqActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private LinearLayout faqContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        initViews();
        setupToolbar();
        setupFaqContent();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        faqContainer = findViewById(R.id.faq_container);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Frequently Asked Questions");
        }
    }

    private void setupFaqContent() {
        // FAQ data
        String[][] faqData = {
            {
                "What is a temporary / disposable / anonymous mail?",
                "Disposable e-mail is a temporary and completely anonymous email address that does not require any registration."
            },
            {
                "Why do you need a temporary email address?",
                "To register on dubious sites without jeopardising your personal data. It is particularly useful for all situations in which your privacy is important."
            },
            {
                "How is it different from usual mail?",
                "• Does not require registration\n• It is completely anonymous: personal data, address itself and emails are deleted after you delete the account\n• Messages are delivered instantly\n• Email address is generated automatically. You do not have to select the available username manually\n• The mailbox is protected from spam, hacking, exploits."
            },
            {
                "How do I send an email?",
                "Unfortunately, we do not provide this feature."
            },
            {
                "How to extend the life time of temporary mail?",
                "You don't need to extend anything, your email account is valid until you'll delete it manually."
            },
            {
                "How long do you store incoming messages?",
                "We do store messages for only 7 days. We're sorry, we can't store them indefinitely."
            },
            {
                "Can I check the received emails?",
                "Yes, they are displayed under the name of your mailbox."
            },
            {
                "Do you keep and renew your domains used by this service?",
                "Yes, we do always keep renewing our domains and they won't disappear."
            },
            {
                "I forgot my password, is there a way you can reset it?",
                "No, there is no way we could reset your password. Your password is being generated in your browser, and it's being stored encrypted."
            }
        };

        // Create FAQ items
        for (String[] faq : faqData) {
            createFaqItem(faq[0], faq[1]);
        }
    }

    private void createFaqItem(String question, String answer) {
        // Create card for each FAQ item
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4);
        card.setRadius(12);
        card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

        // Create main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(16, 16, 16, 16);

        // Create question container (clickable)
        LinearLayout questionContainer = new LinearLayout(this);
        questionContainer.setOrientation(LinearLayout.HORIZONTAL);
        questionContainer.setPadding(0, 0, 0, 12);

        // Create question text
        TextView questionText = new TextView(this);
        questionText.setText(question);
        questionText.setTextSize(16);
        questionText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        questionText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        questionText.setLayoutParams(questionParams);

        // Create expand/collapse indicator
        TextView indicator = new TextView(this);
        indicator.setText("▼");
        indicator.setTextSize(14);
        indicator.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        indicator.setPadding(12, 0, 0, 0);

        questionContainer.addView(questionText);
        questionContainer.addView(indicator);

        // Create answer text (initially hidden)
        TextView answerText = new TextView(this);
        answerText.setText(answer);
        answerText.setTextSize(14);
        answerText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        answerText.setLineSpacing(4, 1.2f);
        answerText.setVisibility(View.GONE);

        // Add click listener to expand/collapse
        questionContainer.setOnClickListener(v -> {
            if (answerText.getVisibility() == View.GONE) {
                answerText.setVisibility(View.VISIBLE);
                indicator.setText("▲");
                indicator.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            } else {
                answerText.setVisibility(View.GONE);
                indicator.setText("▼");
                indicator.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            }
        });

        // Add ripple effect for better UX
        questionContainer.setBackground(ContextCompat.getDrawable(this, android.R.drawable.list_selector_background));

        // Assemble the card
        mainContainer.addView(questionContainer);
        mainContainer.addView(answerText);
        card.addView(mainContainer);

        // Add card to container
        faqContainer.addView(card);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 