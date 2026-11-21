
package com.example.diar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class ChatBaseActivity extends AppCompatActivity {

    private LinearLayout layoutMessages;
    private EditText edtMessage;
    private ScrollView scrollMessages;

    private DatabaseHelper databaseHelper;

    private String etudiantEmail;
    private String etudiantNom;

    private String proprietaireEmail;
    private String proprietaireNom;

    private String currentUserEmail;
    private String currentUserRole;

    private long conversationId = -1;

    protected abstract String getUserRole();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        databaseHelper = new DatabaseHelper(this);

        layoutMessages = findViewById(R.id.layoutMessages);
        edtMessage = findViewById(R.id.edtMessage);
        scrollMessages = findViewById(R.id.scrollMessages);

        findViewById(R.id.btnRetourMessages)
                .setOnClickListener(v -> finish());

        Button btnEnvoyer = findViewById(R.id.btnEnvoyerMessage);

        btnEnvoyer.setOnClickListener(v -> envoyerMessage());

        // Récupérer les informations transmises
        etudiantEmail = getIntent().getStringExtra("etudiantEmail");
        etudiantNom = getIntent().getStringExtra("etudiantNom");

        proprietaireEmail =
                getIntent().getStringExtra("proprietaireEmail");

        proprietaireNom =
                getIntent().getStringExtra("proprietaireNom");

        currentUserRole = getUserRole();

        // Récupérer la session
        currentUserEmail = new SessionManager(this).getEmail();

        if (currentUserEmail == null
                || currentUserEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Session utilisateur introuvable",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Compléter l'email selon le rôle
        if ("ETUDIANT".equalsIgnoreCase(currentUserRole)) {

            etudiantEmail = currentUserEmail;

        } else if ("PROPRIETAIRE".equalsIgnoreCase(currentUserRole)) {

            proprietaireEmail = currentUserEmail;

        } else {

            Toast.makeText(
                    this,
                    "Rôle utilisateur invalide",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Vérifier les deux emails AVANT SQLite
        if (etudiantEmail == null
                || etudiantEmail.trim().isEmpty()
                || proprietaireEmail == null
                || proprietaireEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Informations de conversation manquantes",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Récupérer les noms
        chargerNomsParticipants();

        TextView tvNom = findViewById(R.id.tvNomContact);

        if ("ETUDIANT".equalsIgnoreCase(currentUserRole)) {

            tvNom.setText(
                    proprietaireNom == null
                            ? "Propriétaire"
                            : proprietaireNom
            );

        } else {

            tvNom.setText(
                    etudiantNom == null
                            ? "Étudiant"
                            : etudiantNom
            );
        }

        // Charger ou créer la conversation
        long idRecu = getIntent().getLongExtra(
                "conversationId",
                -1
        );

        if (idRecu > 0) {

            conversationId = idRecu;

        } else {

            conversationId = obtenirOuCreerConversation();
        }

        if (conversationId > 0) {

            chargerMessages();

        } else {

            Toast.makeText(
                    this,
                    "Impossible de créer la conversation",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Récupérer les noms des participants
    private void chargerNomsParticipants() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Nom étudiant
        if (etudiantEmail != null
                && !etudiantEmail.trim().isEmpty()
                && (etudiantNom == null
                || etudiantNom.trim().isEmpty())) {

            Cursor cursor = null;

            try {

                cursor = db.query(
                        "Etudiant",
                        new String[]{"nomComplet"},
                        "email = ?",
                        new String[]{etudiantEmail.trim()},
                        null,
                        null,
                        null
                );

                if (cursor.moveToFirst()) {
                    etudiantNom = cursor.getString(0);
                }

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        // Nom propriétaire
        if (proprietaireEmail != null
                && !proprietaireEmail.trim().isEmpty()
                && (proprietaireNom == null
                || proprietaireNom.trim().isEmpty())) {

            Cursor cursor = null;

            try {

                cursor = db.query(
                        "Proprietaire",
                        new String[]{"nom"},
                        "email = ?",
                        new String[]{proprietaireEmail.trim()},
                        null,
                        null,
                        null
                );

                if (cursor.moveToFirst()) {
                    proprietaireNom = cursor.getString(0);
                }

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    // Obtenir ou créer une conversation
    private long obtenirOuCreerConversation() {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor cursor = db.query(
                "Conversation",
                new String[]{"id"},
                "emailEtudiant = ? AND emailProprietaire = ?",
                new String[]{
                        etudiantEmail,
                        proprietaireEmail
                },
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {

            long id = cursor.getLong(0);

            cursor.close();

            return id;
        }

        cursor.close();

        ContentValues values = new ContentValues();

        values.put("emailEtudiant", etudiantEmail);
        values.put("emailProprietaire", proprietaireEmail);
        values.put("nomEtudiant", etudiantNom);
        values.put("nomProprietaire", proprietaireNom);
        values.put("dateCreation", obtenirDateActuelle());

        long id = db.insertWithOnConflict(
                "Conversation",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        if (id == -1) {

            Cursor recherche = db.query(
                    "Conversation",
                    new String[]{"id"},
                    "emailEtudiant = ? AND emailProprietaire = ?",
                    new String[]{
                            etudiantEmail,
                            proprietaireEmail
                    },
                    null,
                    null,
                    null
            );

            if (recherche.moveToFirst()) {
                id = recherche.getLong(0);
            }

            recherche.close();
        }

        return id;
    }

    // Date actuelle
    private String obtenirDateActuelle() {

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        );

        return format.format(new Date());
    }

    // Envoyer un message
    private void envoyerMessage() {

        String contenu = edtMessage.getText()
                .toString()
                .trim();

        if (contenu.isEmpty()) {

            Toast.makeText(
                    this,
                    "Écrivez un message",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (conversationId <= 0) {

            Toast.makeText(
                    this,
                    "Conversation introuvable",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("conversation_id", conversationId);
        values.put("emailExpediteur", currentUserEmail);
        values.put("contenu", contenu);
        values.put("dateEnvoi", obtenirDateActuelle());
        values.put("lu", 0);

        long resultat = db.insert(
                "Message",
                null,
                values
        );

        if (resultat != -1) {

            edtMessage.setText("");

            chargerMessages();

            Toast.makeText(
                    this,
                    "Message envoyé",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Erreur lors de l'enregistrement du message",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Charger les messages
    private void chargerMessages() {

        if (conversationId <= 0) {
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "Message",
                new String[]{
                        "id",
                        "emailExpediteur",
                        "contenu",
                        "dateEnvoi",
                        "lu"
                },
                "conversation_id = ?",
                new String[]{
                        String.valueOf(conversationId)
                },
                null,
                null,
                "id ASC"
        );

        layoutMessages.removeAllViews();

        try {

            while (cursor.moveToNext()) {

                String sender = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "emailExpediteur"
                        )
                );

                String contenu = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "contenu"
                        )
                );

                boolean messageEnvoye =
                        sender != null
                                && sender.equalsIgnoreCase(currentUserEmail);

                ajouterBulle(contenu, messageEnvoye);
            }

        } finally {

            cursor.close();
        }

        // Marquer les messages reçus comme lus
        ContentValues values = new ContentValues();

        values.put("lu", 1);

        db.update(
                "Message",
                values,
                "conversation_id = ? AND emailExpediteur != ?",
                new String[]{
                        String.valueOf(conversationId),
                        currentUserEmail
                }
        );

        scrollMessages.post(() ->
                scrollMessages.fullScroll(
                        ScrollView.FOCUS_DOWN
                )
        );
    }

    // Afficher les bulles
    private void ajouterBulle(
            String contenu,
            boolean messageEnvoye
    ) {

        TextView bulle = new TextView(this);

        bulle.setText(contenu);
        bulle.setTextSize(14);
        bulle.setTextColor(Color.WHITE);

        bulle.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        GradientDrawable fond = new GradientDrawable();

        fond.setColor(
                messageEnvoye
                        ? Color.parseColor("#1B8A5A")
                        : Color.parseColor("#64748B")
        );

        fond.setCornerRadius(dp(20));

        bulle.setBackground(fond);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.gravity = messageEnvoye
                ? Gravity.END
                : Gravity.START;

        params.topMargin = dp(8);
        params.bottomMargin = dp(8);

        layoutMessages.addView(bulle, params);
    }

    // Conversion DP
    private int dp(int valeur) {

        return (int) (
                valeur * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
