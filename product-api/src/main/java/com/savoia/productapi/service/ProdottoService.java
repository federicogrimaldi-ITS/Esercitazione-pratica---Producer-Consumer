package com.savoia.productapi.service;

import com.savoia.productapi.entity.Prodotto;
import com.savoia.productapi.enums.Categoria;
import com.savoia.productapi.response.ResponseApi;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ProdottoService {

    Prodotto creaProdotto(Prodotto prodotto);

    List<Prodotto> trovaTutti();

    Prodotto trovaPerId(Long id);

    Prodotto aggiornaProdotto(Long id, Prodotto prodotto);

    void eliminaProdotto(Long id);

    List<Prodotto> trovaPerCategoria(Categoria categoria);

    List<Prodotto> cercaPerNome(String nome);

    List<Prodotto> ordinaPerPrezzo(Sort.Direction direzione);
}
