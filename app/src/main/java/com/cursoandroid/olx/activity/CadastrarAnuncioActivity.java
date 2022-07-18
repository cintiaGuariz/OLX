package com.cursoandroid.olx.activity;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.cursoandroid.olx.R;
import com.cursoandroid.olx.helper.ConfiguracaoFirebase;
import com.cursoandroid.olx.helper.Permissoes;
import com.cursoandroid.olx.model.Anuncio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity
implements View.OnClickListener {

    private EditText campoTitulo, campoDefinicao;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaURLFotos = new ArrayList<>();
    private android.app.AlertDialog dialog;

    private Anuncio anuncio;
    private StorageReference storageRef;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cursoandroid.olx.R.layout.activity_cadastrar_anuncio);

        //Configurações iniciais
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();

        carregarDadosSpinner();
    }

    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Salvando anúncio")
                .build();
        dialog.show();

        //Salvar imagem no storage
        for (int i = 0; i < listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }
    }

    private void salvarFotoStorage(String urlString, int totalFotos, int contador){

        //Criar nó no storage
        final StorageReference imagemAnuncio = storageRef.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem" + contador);

        //Fazer upload do arquivo
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagemAnuncio.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri url = task.getResult();
                        String urlConvertida = url.toString();

                        listaURLFotos.add(urlConvertida);

                        if (totalFotos == listaURLFotos.size()){
                            anuncio.setFotos(listaURLFotos);
                            anuncio.salvar();

                            dialog.dismiss();
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Erro ao fazer upload");
                Log.i("INFO", "Falha ao fazer upload" + e.getMessage());
            }
        });
    }

    private Anuncio configurarAnuncio(){

        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getMasked();
        String descricao = campoDefinicao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setValor(valor);
        anuncio.setTitulo(titulo);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);

        return anuncio;
    }

    private void carregarDadosSpinner(){

        //Configura spinner estado
        String[] estados = getResources().getStringArray(R.array.estados);
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapterEstado);

        //Configura spinner categoria
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);
    }

    public void validarDadosAnuncio(View view){

        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if (listaFotosRecuperadas.size() != 0){
            if (!anuncio.getTitulo().isEmpty()){
                if (!valor.isEmpty() && !valor.equals("0")){
                    if (!anuncio.getTelefone().isEmpty() && anuncio.getTelefone().length() >= 10){
                        if (!anuncio.getDescricao().isEmpty()){
                            salvarAnuncio();

                        }else {
                            exibirMensagemErro("Preencha a descrição!");
                        }
                    }else {
                        exibirMensagemErro("Preencha o telefone!");
                    }
                }else {
                    exibirMensagemErro("Preencha o valor!");
                }
            }else {
                exibirMensagemErro("Preencha o título!");
            }
        }else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }

    }

    private void exibirMensagemErro(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            //Recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if (requestCode == 1){
                imagem1.setImageURI(imagemSelecionada);
            }else if (requestCode == 2){
                imagem2.setImageURI(imagemSelecionada);
            }else if (requestCode == 3){
                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
               alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissão negada");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inicializarComponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDefinicao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener((View.OnClickListener) this);
        imagem2.setOnClickListener((View.OnClickListener) this);
        imagem3.setOnClickListener((View.OnClickListener) this);

        //Configura localidade para pt - Brasil
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale(locale);
    }


}