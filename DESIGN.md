# Suraksha Jyoti — design system (Phase 1)

Concept: a diya flame inside a jaali-lattice shield. Protection is on. Warm, protective, festive, Indian.
Colour: ink #12101F, surface #1C1932, surface-high #26224A, text #F4EFE6, muted #A59FB8; light paper #FBF4E8, text #1B1730.
Accent saffron #FF8A1F appears once per screen: the flame or the main button, never both.
Verdicts: neem #1FA37A (no clear danger), haldi #F2B632 (careful), sindoor #E5383B (danger); each carries its own icon and word, never colour alone.
Spacing: 4dp scale 4/8/12/16/24/32/48/64. Radii: cards 28dp on two corners and 8dp on the others; buttons pill; 1dp lattice borders; 48dp minimum touch.
Type: Yatra One for display with a Mukta fallback for Devanagari body; Mukta 400/600/800; ratio 1.25; verdict word 56sp.
Icons: hand-built ImageVectors, 2dp strokes on a 24dp grid (shield, flame, link, chat, call, camera, family, book, lamp).
Texture: procedural jaali and rangoli on Canvas at 6-8% opacity over subtle paper grain; never a flat fill; no image assets, no emoji.
Motion: 120/200/320/520ms; entrance cubic-bezier(0.2,0.8,0.2,1); press spring damping 0.7 stiffness 400, scale 0.97, light haptic; stagger 70ms.
Hero states: Idle (calm saffron), Scanning (flame leans, 24 ring segments light one by one, minimum 800ms), Clear (steady neem), Careful (haldi waver), Dangerous (sindoor tremble, two screen-edge pulses, strong haptic).
Signature: ink-spread into Result, odometer score, lattice ripple on the main button, flame parallax toward touch, pull-down lights the lamp, flame nav indicator, long-press a reason for a mini-explainer.
Discipline: at most two infinite animations per screen, graphicsLayer transforms, no full-screen blur, honour "remove animations", in-app Calm mode, sound off by default with three tones synthesised in code.
Avoid: purple-blue gradients, glassmorphism, identical card grids, emoji icons, Inter/Roboto, marketing buzzwords, em dashes.
