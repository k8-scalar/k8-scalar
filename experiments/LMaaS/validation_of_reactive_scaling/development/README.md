# Experiment 2
Name: 		Validatie van het proof-of-concept schalingssysteem ARBA

## Description:
In dit experiment wordt er nagegaan dat ARBA correct werkt.
Hiervoor worden verzoeken verstuurd totdat een arbitraire drempel overschreden wordt.
Er wordt voor een setup van één Cassandra gekozen met een drempel van 0.7 * totaal millicores cpu.

## Hypothese
Na het overschreiden van deze drempel wordt er een instantie toegevoegd
