package com.savoia.productapi.payload.request;

import com.savoia.productapi.entity.Prodotto;
import com.savoia.productapi.enums.Categoria;
import com.savoia.productapi.utils.StringUtility;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdottoRequest {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 200, message = "Il nome non può superare i 200 caratteri")
    private String name;

    private String description;

    @NotNull(message = "Il prezzo è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Digits(integer = 8, fraction = 2, message = "Il prezzo deve avere al massimo 8 cifre intere e 2 decimali")
    private BigDecimal price;

    @NotNull(message = "La categoria è obbligatoria")
    private Categoria category;

    @Builder.Default
    @Min(value = 0, message = "La quantità non può essere negativa")
    private Integer quantity = 0;

    public static Prodotto mapToEntity(ProdottoRequest prodottoRequest){
        return Prodotto.builder()
                .name(formatText(prodottoRequest.getName()))
                .description(formatText(prodottoRequest.getDescription()))
                .price(prodottoRequest.getPrice())
                .category(prodottoRequest.getCategory())
                .quantity(prodottoRequest.getQuantity() == null ? 0 : prodottoRequest.getQuantity())
                .build();
    }

    private static String formatText(String text) {
        return StringUtility.capitalizeFirstLetter(StringUtility.cleanString(text));
    }
}
