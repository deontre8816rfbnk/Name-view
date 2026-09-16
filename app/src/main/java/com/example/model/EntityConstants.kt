package com.example.model

/**
 * eFootball-inspired positions, stats, playstyles, and skills.
 * Single source of truth for the app.
 */
object EntityConstants {

    val POSITIONS = listOf(
        "GK", "CB", "LB", "RB", "DMF", "CMF", "AMF",
        "LMF", "RMF", "LWF", "RWF", "SS", "CF"
    )

    /** Stats shown for each primary position. IQ is always appended. */
    fun statsForPosition(position: String): List<String> {
        val base = when (position.uppercase()) {
            "CF", "SS" -> listOf(
                "Offensive Awareness", "Finishing", "Kicking Power", "Heading",
                "Speed", "Acceleration",
                "Physical Contact", "Jumping", "Balance", "Stamina",
                "Ball Control", "Dribbling", "Tight Possession", "Low Pass", "Lofted Pass", "Curl",
                "Weak Foot Usage", "Weak Foot Accuracy", "Form", "Injury Resistance"
            )
            "CMF" -> listOf(
                "Low Pass", "Lofted Pass", "Curl",
                "Ball Control", "Dribbling", "Tight Possession",
                "Acceleration", "Speed", "Balance", "Stamina",
                "Defensive Engagement", "Tackling", "Aggression", "Defensive Awareness",
                "Offensive Awareness", "Kicking Power", "Finishing"
            )
            "AMF" -> listOf(
                "Low Pass", "Lofted Pass", "Ball Control", "Dribbling", "Tight Possession",
                "Offensive Awareness", "Finishing", "Kicking Power", "Curl",
                "Acceleration", "Balance", "Speed", "Stamina"
            )
            "LMF", "LWF", "RMF", "RWF" -> listOf(
                "Acceleration", "Speed", "Balance", "Stamina",
                "Dribbling", "Ball Control", "Tight Possession", "Lofted Pass", "Curl",
                "Offensive Awareness", "Finishing", "Kicking Power",
                "Defensive Engagement", "Aggression"
            )
            "CB" -> listOf(
                "Defensive Awareness", "Tackling", "Aggression", "Defensive Engagement",
                "Heading", "Jumping", "Physical Contact", "Height",
                "Speed", "Acceleration", "Low Pass", "Lofted Pass"
            )
            "LB", "RB" -> listOf(
                "Acceleration", "Speed", "Balance", "Stamina",
                "Defensive Awareness", "Tackling", "Aggression", "Defensive Engagement", "Physical Contact",
                "Lofted Pass", "Low Pass", "Dribbling", "Ball Control", "Tight Possession", "Curl"
            )
            "DMF" -> listOf(
                "Defensive Awareness", "Tackling", "Aggression", "Defensive Engagement",
                "Physical Contact", "Balance", "Stamina",
                "Low Pass", "Lofted Pass", "Ball Control",
                "Speed", "Acceleration", "Heading", "Jumping"
            )
            "GK" -> listOf(
                "GK Awareness", "GK Catching", "GK Parrying", "GK Reflexes", "GK Reach",
                "Height", "Jumping", "Physical Contact", "Kicking Power", "Low Pass", "Lofted Pass"
            )
            else -> listOf(
                "Offensive Awareness", "Finishing", "Speed", "Acceleration",
                "Physical Contact", "Stamina", "Ball Control", "Dribbling"
            )
        }
        return base + "IQ"
    }

    /** Short explanation for each stat title click. */
    fun statExplanation(statName: String): String {
        return when (statName) {
            "Offensive Awareness" -> "How quickly the player reacts, makes runs, and positions in attack."
            "Finishing" -> "Accuracy and consistency of shots inside and around the box."
            "Kicking Power" -> "Shot velocity and power."
            "Heading" -> "Precision and strength of aerial headers."
            "Speed" -> "Top sprinting speed over distance."
            "Acceleration" -> "How quickly top speed is reached on burst runs."
            "Physical Contact" -> "Ability to hold off opponents and win physical duels."
            "Jumping" -> "Vertical jump height for high balls."
            "Balance" -> "Stability under pressure or while turning."
            "Stamina" -> "Energy retention through the match."
            "Ball Control" -> "First touch quality when receiving passes."
            "Dribbling" -> "Control while moving with the ball."
            "Tight Possession" -> "Turning and maneuvering in tight spaces."
            "Low Pass" -> "Quality of short ground passes."
            "Lofted Pass" -> "Quality of lofted / crossing passes."
            "Curl" -> "Bending shot and pass capability."
            "Weak Foot Usage" -> "How often the weak foot is used."
            "Weak Foot Accuracy" -> "Accuracy when using the weak foot."
            "Form" -> "Consistency of pre-match condition."
            "Injury Resistance" -> "Resistance to injuries."
            "Defensive Awareness" -> "Reading of defensive situations and positioning."
            "Tackling" -> "Success and timing of tackles."
            "Aggression" -> "Intensity in challenges and pressing."
            "Defensive Engagement" -> "Willingness and timing of defensive actions."
            "Height" -> "Physical height (affects aerial reach)."
            "GK Awareness" -> "Positional reading and reaction to loose balls."
            "GK Catching" -> "How cleanly shots are held."
            "GK Parrying" -> "Ability to deflect saves safely."
            "GK Reflexes" -> "Response time for close-range or deflected shots."
            "GK Reach" -> "Range and angle coverage on long or wide shots."
            "IQ" -> "Game intelligence, decision-making and positioning sense."
            else -> "Stat value guide: ~70 capable · ~80 good · ~90 expert · 100 elite."
        }
    }

    fun playstylesForPosition(position: String): List<String> {
        return when (position.uppercase()) {
            "CF" -> listOf("Goal Poacher", "Fox in the Box", "Target Man", "Deep-Lying Forward", "Dummy Runner")
            "SS" -> listOf("Creative Playmaker", "Roaming Flank", "Hole Player", "Second Striker")
            "AMF" -> listOf("Classic No. 10", "Hole Player", "Creative Playmaker", "Orchestrator")
            "CMF" -> listOf("Box-to-Box", "Orchestrator", "The Destroyer", "Anchor Man", "Hole Player")
            "DMF" -> listOf("Anchor Man", "The Destroyer", "Deep-Lying Playmaker", "Box-to-Box")
            "LMF", "RMF" -> listOf("Cross Specialist", "Roaming Flank", "Prolific Winger", "Full-back Finish")
            "LWF", "RWF" -> listOf("Prolific Winger", "Roaming Flank", "Cross Specialist", "Goal Poacher")
            "CB" -> listOf("Build Up", "Extra Frontman", "The Destroyer", "Defensive Full-back")
            "LB", "RB" -> listOf("Offensive Full-back", "Defensive Full-back", "Full-back Finish", "Cross Specialist")
            "GK" -> listOf("Offensive Goalkeeper", "Defensive Goalkeeper")
            else -> listOf("Balanced")
        }
    }

    val PLAYER_SKILLS = listOf(
        "Ghost Run",
        "Toe-Poke Snip",
        "Hammer Volley",
        "Ankle-Breaker Pivot",
        "Blind-Side Flop",
        "Curtain Curve"
    )

    val MANAGER_SKILLS = listOf(
        "Form Shifter",
        "Tactical Shapeshifter",
        "Gegenpress Catalyst",
        "Master Subcontractor",
        "Park-the-Bus Architect"
    )

    val MANAGER_STATS = listOf(
        "Tactical Adaptability",
        "Touchline Motivation",
        "Man Management",
        "Youth Development",
        "IQ"
    )

    val VALUE_GUIDE = "~70 capable · ~80 good · ~90 expert · 100 elite / own style"
}
