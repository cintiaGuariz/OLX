package com.cursoandroid.olx.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cursoandroid.olx.R;
import com.cursoandroid.olx.model.Anuncio;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetalhesProdutoActivity extends AppCompatActivity {

    private ImageSlider imageSlider;
    private TextView titulo, estado, preco, descricao;
    private Anuncio anuncioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cursoandroid.olx.R.layout.activity_detalhes_produto);

        //Configurar toolbar
        getSupportActionBar().setTitle("Detalhe produto");

        inicializarComponentes();

        //Recuperar anúncio para exibição
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if (anuncioSelecionado != null){

            titulo.setText(anuncioSelecionado.getTitulo());
            estado.setText(anuncioSelecionado.getEstado());
            preco.setText(anuncioSelecionado.getValor());
            descricao.setText(anuncioSelecionado.getDescricao());

            List<SlideModel> slideModels = new ArrayList<>();

            if (anuncioSelecionado.getFotos().size() == 3){
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(0), ScaleTypes.CENTER_INSIDE));
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(1), ScaleTypes.CENTER_INSIDE));
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(2), ScaleTypes.CENTER_INSIDE));
            }else if (anuncioSelecionado.getFotos().size() == 2){
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(0), ScaleTypes.CENTER_INSIDE));
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(1), ScaleTypes.CENTER_INSIDE));
            }else {
                slideModels.add(new SlideModel(anuncioSelecionado.getFotos().get(0), ScaleTypes.CENTER_INSIDE));
            }

            imageSlider.setImageList(slideModels);
        }

    }

    public void vizualizarTelefone(View view){
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anuncioSelecionado.getTelefone(), null));
        startActivity(i);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        imageSlider = findViewById(R.id.imageSlider);
        titulo = findViewById(R.id.textTituloDetalhe);
        estado = findViewById(R.id.textEstadoDetalhe);
        preco = findViewById(R.id.textPrecoDetalhe);
        descricao = findViewById(R.id.textDescricaoDetalhe);
    }
}