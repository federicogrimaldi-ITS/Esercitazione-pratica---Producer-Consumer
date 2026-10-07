package com.savoia.productclient.service;

import com.savoia.productclient.client.ProdottoApiClient;
import com.savoia.productclient.config.ApiProperties;
import com.savoia.productclient.model.Categoria;
import com.savoia.productclient.model.CriteriRicerca;
import com.savoia.productclient.model.ProdottoDTO;
import com.savoia.productclient.model.ProductForm;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logica applicativa del Consumer: i dati arrivano esclusivamente dalla Producer API.
 */
@Service
public class ProdottoService {

    private final ProdottoApiClient prodottoApiClient;
    private final List<Categoria> categories;

    public ProdottoService(ProdottoApiClient prodottoApiClient, ApiProperties apiProperties) {
        this.prodottoApiClient = prodottoApiClient;
        this.categories = apiProperties.categories().stream().map(Categoria::new).toList();
    }

    public List<ProdottoDTO> cercaProdotti(CriteriRicerca criteri) {
        return prodottoApiClient.trovaTutti(criteri);
    }

    public ProdottoDTO trovaProdotto(Long id) {
        return prodottoApiClient.trovaPerId(id);
    }

    public List<Categoria> categorie() {
        return categories;
    }

    public ProdottoDTO createProduct(ProductForm productForm) {
        return prodottoApiClient.createProduct(productForm);
    }

    public ProdottoDTO updateProduct(Long id, ProductForm productForm) {
        return prodottoApiClient.updateProduct(id, productForm);
    }

    public void deleteProduct(Long id) {
        prodottoApiClient.deleteProduct(id);
    }
}
