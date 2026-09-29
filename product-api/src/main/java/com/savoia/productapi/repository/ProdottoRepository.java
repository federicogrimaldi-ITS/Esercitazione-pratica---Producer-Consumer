package com.savoia.productapi.repository;

import com.savoia.productapi.entity.Prodotto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {
}
