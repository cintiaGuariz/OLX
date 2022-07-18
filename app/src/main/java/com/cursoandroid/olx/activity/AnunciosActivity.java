package com.cursoandroid.olx.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.cursoandroid.olx.R;
import com.cursoandroid.olx.adapter.AdapterAnuncios;
import com.cursoandroid.olx.helper.ConfiguracaoFirebase;
import com.cursoandroid.olx.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private Button buttonRegiao, buttonCategoria;
    private android.app.AlertDialog dialog;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cursoandroid.olx.R.layout.activity_anuncios);

        inicializarComponentes();

        //Configura o recycler view
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        recuperarAnunciosPublicos();

    }

    public void recuperarAnunciosPublicos(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Carregando anúncios")
                .build();
        dialog.show();

        listaAnuncios.clear();
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot estados : snapshot.getChildren()){
                    for (DataSnapshot categorias : estados.getChildren()){
                        for (DataSnapshot anuncios : categorias.getChildren()){

                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (autenticacao.getCurrentUser() == null){//usuário deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);

        }else {//usuário logado
            menu.setGroupVisible(R.id.group_logado, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_cadastrar :
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;

            case R.id.menu_sair :
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;

            case R.id.menu_anuncios :
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void inicializarComponentes(){
        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
    }
}