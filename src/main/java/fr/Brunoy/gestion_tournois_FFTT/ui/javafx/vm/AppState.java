package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.vm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {

    public record TableauItem(String code, String label, int priceEuros) {}

    private final ObservableList<TableauItem> available = FXCollections.observableArrayList(
            new TableauItem("A", "Tableau A - Classique", 6),
            new TableauItem("B", "Tableau B - Classé", 7),
            new TableauItem("C", "Tableau C - Jeunes", 5),
            new TableauItem("D", "Tableau D - Féminin", 5)
    );

    private final ObservableList<TableauItem> selected = FXCollections.observableArrayList();

    public ObservableList<TableauItem> available() { return available; }
    public ObservableList<TableauItem> selected() { return selected; }

    public int totalPrice() {
        return selected.stream().mapToInt(TableauItem::priceEuros).sum();
    }
}
