package com.savoia.productclient.service;

import com.savoia.productclient.client.ProdottoApiClient;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Logica applicativa del Consumer: i dati arrivano esclusivamente dalla Producer API.
 */
@Service
public class ProdottoService {

    private final ProdottoApiClient prodottoApiClient;

    public ProdottoService(ProdottoApiClient prodottoApiClient) {
        this.prodottoApiClient = prodottoApiClient;
    }

    public List<ProdottoDTO> cercaProdotti(CriteriRicerca criteri) {
        return prodottoApiClient.trovaTutti(criteri);
    }

    public ProdottoDTO trovaProdotto(Long id) {
        return prodottoApiClient.trovaPerId(id);
    }

    /**
     * Categorie distinte presenti nel catalogo, ordinate alfabeticamente (per il filtro della ricerca).
     */
    public List<Categoria> categorie() {
        return prodottoApiClient.trovaTutti(CriteriRicerca.nessuno()).stream()
                .map(ProdottoDTO::categoria)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(Categoria::new)
                .toList();
    }
}
