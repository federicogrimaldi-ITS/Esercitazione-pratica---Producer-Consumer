package com.savoia.productapi.service;

import com.savoia.productapi.entity.Prodotto;
import com.savoia.productapi.enums.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdottoServiceImpl implements ProdottoService{

    private final ProdottoServiceImpl prodottoService;

    @Override
    public Prodotto creaProdotto(Prodotto prodotto) {
        return null;
    }

    @Override
    public List<Prodotto> trovaTutti() {
        return List.of();
    }

    @Override
    public Prodotto trovaPerId(Long id) {
        return null;
    }

    @Override
    public Prodotto aggiornaProdotto(Long id, Prodotto prodotto) {
        return null;
    }

    @Override
    public void eliminaProdotto(Long id) {

    }

    @Override
    public List<Prodotto> trovaPerCategoria(Categoria categoria) {
        return List.of();
    }

    @Override
    public List<Prodotto> cercaPerNome(String nome) {
        return List.of();
    }

    @Override
    public List<Prodotto> ordinaPerPrezzo(Sort.Direction direzione) {
        return List.of();
    }
}
