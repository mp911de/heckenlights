<?php

$presetfiles = array(
    "AlleJahreWieder.mid" => "Alle Jahre wieder",
    "DeckTheHalls.mid" => "Deck the Halls",
    "JingleBells.mid" => "Jingle Bells",
    "LeiseRieseltDerSchnee.mid" => "Leise rieselt der Schnee",
    "OComeAllYeFaithful.mid" => "O Come, All Ye Faithful",
    "WeWishYouAMerryChristmas.mid" => "We Wish You a Merry Christmas",
    "Adagio.mid" => "Adagio for Strings",
    "Heitschi.mid" => "Åber heidschi bumbeidschi",
    "KommetIhrHirten.mid" => "Kommet ihr Hirten",
    "Toccata.mid" => "Toccata and Fugue in D Minor"
);

$randompresets = array();
$randomkeys = array_rand($presetfiles, 4);
foreach ($randomkeys as $key => $value) {
    $randompresets[$value] =  $presetfiles[$value];
}
asort($randompresets);
$presets = $randompresets;
?>