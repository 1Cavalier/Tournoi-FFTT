package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

public record PrizeTierDto(

                Integer fromRank,
                Integer toRank,

                PrizeRewardTypeDto rewardType,

                Integer cashAmount,
                Integer registrationDiscountPercent) {
}