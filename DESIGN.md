# BharatSurakshaSaathi — design system "Saathi"

Concept: a calm village elder who guards you. Warm, protective, a little festive.
Palette — dark ink #12101F, surface #1C1932, surface-high #26224A, text #F4EFE6, muted #A59FB8.
Palette — light paper #FBF4E8, surface #FFFFFF, text #1B1730, muted #6B6580.
Accent saffron #FF8A1F, rare: the single primary action OR the shield glow, never both on one screen.
Verdicts: neem #1FA37A (no clear warning signs), haldi #F2B632 (careful), sindoor #E5383B (dangerous); each carries its own icon shape and a word, never colour alone.
Type: Yatra One for display (verdict word 56sp), Mukta 400/600/800 for body and headings; scale ratio 1.25; headings left-aligned and heavy, body light.
Shape: cards 28dp top-left + bottom-right with 8dp opposite corners, buttons fully pill, 1dp lattice-line borders, deliberately asymmetric layouts (offset hero, overlap, left-aligned big type).
Motion: one staggered entrance per screen (70ms offsets, cubic-bezier(0.2, 0.8, 0.2, 1)); shield breathes 1.00 to 1.03 over 4s inside a slow chakra ring; at most two infinite animations per screen; honour "remove animations".
Texture: jaali lattice drawn on Compose Canvas at ~7% opacity over a soft radial saffron glow behind the shield; never a flat fill, no image assets, no emoji icons.
