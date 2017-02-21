
# Match

A library for fuzzy matching of place names and dates.

## LatinPlaceNameScorer

The [LatinPlaceNameScorer](src/main/java/org/activityinfo/io/match/names/LatinPlaceNameScorer.java) 
helps identify pairs of place names, written in a Latin script, 
that are likely to be the same, but transliterated differently.

These differences can arise from a number of random processes:

1. The same sound may be written differently, perhaps because
   of a different transliteration scheme, for example:

    * [ou]adi ⇒ [w]adi
    * zou[q] bha[nn]ine ⇒ zou[k] bha[n]ine
    * z[ai]toun ⇒ z[ei]toun[e]

2. Sounds can drift regionally and over time, and these
   differences result in new spellings

3. Names that include parts of speech can be reordered or
   discarded arbitrarily:

    * "Santa Rosa City" ⇒ "City of Santa Rosa"
    * "Commune de Goumera" ⇒ "Goumera"

4. Words can be split or joined

    * "Bara Sara" ⇒ "Barassara"
    * "Nema Badenyakafo" ⇒ "Nema Badenya Kafo"

In order to be useful in matching large data sets, we need 
a low false positive rate, otherwise analysts will be overwhelmed with
hundreds or thousands of garbage matches to review.

Traditional edit-distance metrics, such as
[Levenshtein Distance](https://en.wikipedia.org/wiki/Levenshtein_distance) or
[Jaro-Winkler similarity](https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance)
tend to produce far too many false positives. The strings `AAIN` and `ZEBDINE`, for example,
have a Jaro-Winkler similarity of 0.60, despite there being no chance that
`ZEBDINE` is an alternate spelling of `AAIN`.

The LatinPlaceNameScorer considers instead the probability that `AAIN` could
be transformed into `ZEBDINE` by the processes listed above. Because 
we consider addition of consonants like `Z`, `B`, `D` extremely unlikely,
the Scorer returns a value of zero. 

On the other hand, the string `AIN` differs only in a single vowel, which
is very common difference in transliteration, resulting in a score of 
0.925.


### Algorithm 

The algorithm works as follows:

First, each input string is upper-cased and stripped of any diacritical marks,
including apostrophes when found between letters. For example:

* "Aïn-El-Jdeidé" ⇒ `AIN-EL-JDEIDE`
* "Aïn Kéni" ⇒ `AIN KENI`
* "N'Goutjina" ⇒ `NGOUTJINA`

Next, each input string is split into one or more "parts". All symbols such 
as "-", "/", "(", etc, are considered to start new parts, and digits will
start a new part if preceded by characters. For example:

* `AIN-EL-JDEIDE` ⇒ [`AIN`, `EL`, `JDEIDE`]
* `JUBBADA HOOSE (LOWER JUBA)` ⇒ [`JUBBADA`, `HOOSE`, `LOWER`, `JUBA`]
* `DOUGOUTENE 2` ⇒ [`DOUGOUTENE`, `2`]
* `2EME FORCE NAVALE` ⇒ [`2EME`, `FORCE`, `NAVALE`]

Given two input strings each broken into an array of parts, for example,
[`COMMUNE`, `DE`, `GOUMERA`] and [`GOUMERA`, `COMUNE`], we compute a similarity score for each 
pair of parts from the left and right:

|        | GOUMERA | COMUNE   |
|--------|--------:|---------:|
|COMMUNE |     0.0 |  **0.93**|
|DE      |     0.0 |     0.00 |
|GOUMERA | **1.0** |     0.00 |

The similarity score between two parts is computed depending on the type
of parts:

* If both parts are numbers, the numbers are compared exactly. "1" and "2" have 
  a similarity score of 0.0, while "1" and "1" have a score of 1.0. 
* If both parts are alphabetic, the phonetic similarity score is computed. (See below)
* If one part is numeric and the other alphabetic, we check to see if the 
  alphabetic part is a roman numeral and then compare exactly with the number.
  Otherwise the two parts have a similarity of zero. 
  
With the similarity matrix in hand, we take the best score of all permutations
of the left and right parts, weighted by part length. In the example above,
we would consider six permutations:

* [COMMUNE, GOUMERA] ⇔ [COMUNE, DE] = 0.37
* [COMMUNE, GOUMERA] ⇔ [COMUNE, GOUMERA] = 0.86 (missing "DE")
* [COMMUNE, GOUMERA] ⇔ [DE, COMUNE] = 0
* [COMMUNE, GOUMERA] ⇔ [DE, GOUMERA] = 0.24
* [COMMUNE, GOUMERA] ⇔ [GOUMERA, DE] = 0

The best permutation is the second one, where the order of 
[GOUMERA, COMUNE] is flipped.

### Phonetic Similarity Algorithm

When computing the similarity of two alphabetic parts, the
[LatinWordDistance](src/main/java/org/activityinfo/io/match/names/LatinWordDistance.java)
algorithm is used to compute the distance between two parts, assumed to be
words in Latin script.

Like the Levenstein algorithm, we compute the minimum number of "edits" --
substitutions, insertions, and deletions -- but instead of treating
every character difference equally, each "edit" is assigned a cost based 
on the likelihood it resulted from a transliteration difference.

For example, vowels are often transliterated differently, so extra vowels 
or vowel substitutions are assigned a relatively low cost. 
In the case of `MREISSE` and `MRAISSE`, for example, substitution of
 `E` with `A` is assigned a cost of 0.25.

On the other hand, most consonant substitutions are very rare: the word
`RAS` is very unlikely to be transliterated as `RAB`, and the algorithm
assigns an infinite cost to this substitution. 

Some consonant substitutions _are_ common, including `M` → `N` and
`K` → `Q`, and are assigned a cost of 0.5 and 0.25 respectively.

Additional rules have been developed based on datasets encountered in
Afghanistan, [Lebanon](src/test/resources/org/activityinfo/io/match/names/lebanon.txt),
[Mali](src/test/resources/org/activityinfo/io/match/names/mali.txt), and
[Philipines](src/test/resources/org/activityinfo/io/match/names/philipines.txt).

## LatinDateParser

The [LatinDateParser](src/main/java/org/activityinfo/io/match/date/LatinDateParser.java) 
parses date strings in the Latin Script in some unknown locale in
an unknown format.

