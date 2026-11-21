
package com.example.diar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class interface1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface1);

        // Création de la base de données SQLite
        DatabaseHelper databaseHelper =
                new DatabaseHelper(this);

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        // Navigation vers l'écran de connexion
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(
                    interface1.this,
                    Interface3.class
            );
            startActivity(intent);
            finish();
        }, 3000);
    }
}
