package org.eraser.fuckgboard.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FlagHook(private val prefs: XSharedPreferences?) {

    private val flagNameFieldCache = ConcurrentHashMap<Class<*>, Field>()

    companion object {
        private const val TAG = "PixelBoard-Flags"
        private val overrideLogCount = AtomicInteger(0)

        // Known flag wrapper classes across Gboard versions:
        // 18.5.2 Beta: adbx
        // 18.4.1 Release: odx
        // 18.3.1 Release: obw
        // 18.3.1 Beta: acps
        // 18.0.3: nxw
        // 17.8.4: ajoh
        private val KNOWN_FLAG_CLASSES = listOf(
            "adbx",
            "odx",
            "obw",
            "acps",
            "nxw",
            "ajoh"
        )

        // Gemini Rambler & NGA voice typing flags
        private val RAMBLER_VOICE_BOOLEAN_FLAGS = setOf(
            "enable_nga",
            "enable_advanced_features_in_consolidated_sd_stack",
            "enable_dictation_feature_split_install",
            "handle_fallback_inside_sd_stack",
            "immediately_end_dictation_on_keyboard_hidden",
            "enable_soda_longform_experiment",
            "show_rambler_dict_settings",
            "enable_agentic_dictation",
            "config_agentic_dictation",
            "enable_jetson_in_toolbar",
            "filter_rambler_contributed_input_view_session",
            "enable_rambler_toolbar_at_cursor_position",
            "enable_rambler_al_toolbar",
            "enable_rambler_al_toolbar_overflow_menu",
            "enable_hide_rambler_al_idling_state",
            "enable_rambler_al_show_hide_new_animation",
            "enable_rambler_al_teleporting_animation",
            "rambler_motion_with_gradient_color",
            "enable_rambler_motion_dots",
            "rambler_preserve_rewritten_text_on_revert",
            "agentic_dictation_spot_light2",
            "agentic_dictation_new_gi_motion",
            "enable_jetson_s3_quota_experiment_override",
            "agentic_dictation_disable_pre_session_quota_check",
            "enable_pcm_audio_caching_for_offline_fallback",
            "agentic_dictation_enable_seamless_fallback",
            "agentic_dictation_enable_proactive_parallel_asr"
        )

        // AI Writing Tools Core
        private val AI_WRITING_TOOLS_CORE_FLAGS = setOf(
            "writing_helper",
            "config_proofread",
            "proofread",
            "writing_tools",
            "writing_tools_enable_stable_entrance",
            "enable_super_icon_in_dynamic_writing_tools",
            "enable_writing_tools_replace_button",
            "enable_writing_tools_compact_layout",
            "enable_writing_tools_scroll_hint_anim",
            "writing_helper_chip_in_spellchecker",
            "writing_helper_on_selected_text",
            "writing_helper_enable_text_stylization_internal",
            "enable_writing_tools_cooperative_mode",
            "enable_writing_tools_for_minors",
            "enable_nga_lab_modeless_smartedit",
            "enable_writing_tools_voice_commands"
        )

        // Writing Tools V2 (Pixel 9+ feature set)
        private val AI_WRITING_TOOLS_V2_FLAGS = setOf(
            "enable_writing_tools_v2",
            "enable_writing_tools_v2_on_toolbar",
            "enable_writing_tools_suggest_style",
            "writing_tools_v2",
            "writing_tools_v2_enable_suggested_instructions",
            "writing_tools_v2_enable_suggested_instructions_in_draft_response",
            "writing_tools_v2_display_instruction_suggestions_in_zero_state",
            "writing_tools_v2_allow_empty_user_instruction",
            "writing_tools_v2_show_predefined_prompts",
            "enable_writing_tools_my_style",
            "enable_writing_tools_my_style_plus"
        )

        // Material 3 Belka Rounded Keys flags
        private val ROUNDED_KEYS_FLAGS = setOf(
            "belka_rounded_keyboard",
            "belka_ui",
            "belka_background",
            "belka_center_key",
            "top_rounded_keyboard",
            "always_top_rounded_keyboard_for_debug",
            "more_pill_keys",
            "enable_rounded_key_by_default",
            "enable_rounded_keys"
        )

        // Non-functional UI & Appearance flags
        private val UI_STYLING_FLAGS = setOf(
            "emoji_scale_supported",
            "enable_access_points_menu_redesign",
            "enable_access_point_keyboard",
            "enable_spellchecker_chips_ui",
            "enable_spellchecker_chips_ui_web_view"
        )

        // Functional Typing, Learning, and Intelligence flags
        private val TYPING_AND_EXPRESSION_FLAGS = setOf(
            "enable_device_intelligence",
            "enable_autofill_ime_integration",
            "enable_spell_checker_extension",
            "enable_pc_user_history_ngram_count",
            "populate_ngram_count_with_context",
            "populate_ngram_timestamp_in_user_history",
            "enable_multiword_predictions_from_user_history",
            "enable_multiword_suggestions_from_user_history",
            "enable_dynamic_lm_unigram_continuation_scoring",
            "preserve_context_in_dynamic_lm_compute_history",
            "enable_dynamic_lm_noise_filter",
            "enable_oov_deduplication",
            "config_expression",
            "enable_concept_prediction_engine",
            "enable_rule_based_concept_prediction",
            "enable_last_word_rule_based_concept_prediction",
            "enable_suffix_rule_based_concept_prediction",
            "enable_emoji_suggestion_from_top_candidate",
            "enable_predictions_from_emoji_shortcuts",
            "enable_ngram_emoji_shortcuts_by_spans",
            "enable_ngram_emoji_shortcuts_for_prediction",
            "retain_emoji_shortcut_candidates_on_selection",
            "update_streaming_touch_data_set_is_gesture",
            "enable_two_stage_decoder",
            "enable_space_transposition_post_correction",
            "enable_pk_spatial_model_features",
            "enable_pk_auto_correction_trigger_predictor",
            "enable_post_correction_trigger_predictor",
            "populate_preemptive_next_key_predictions"
        )
    }

    private var lastReloadTime = 0L

    private fun checkReload() {
        val now = System.currentTimeMillis()
        if (now - lastReloadTime > 5000L) {
            lastReloadTime = now
            try {
                prefs?.reload()
            } catch (_: Throwable) {}
        }
    }

    private fun getBool(key: String, def: Boolean): Boolean {
        checkReload()
        return try {
            prefs?.getBoolean(key, def) ?: def
        } catch (_: Throwable) {
            def
        }
    }

    private fun getString(key: String, def: String): String {
        checkReload()
        return try {
            prefs?.getString(key, def) ?: def
        } catch (_: Throwable) {
            def
        }
    }

    private fun logOverride(flagName: String, value: Any?) {
        val count = overrideLogCount.getAndIncrement()
        if (count < 50) {
            XposedBridge.log("[$TAG] Override: $flagName -> $value")
        }
    }

    fun apply(classLoader: ClassLoader) {
        var hooked = false
        for (className in KNOWN_FLAG_CLASSES) {
            try {
                val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: continue
                hookFlagClass(clazz)
                hooked = true
                XposedBridge.log("[$TAG] Successfully hooked flag class: $className")
                break
            } catch (t: Throwable) {
                // ignore
            }
        }

        // Dynamic fallback detection
        if (!hooked) {
            try {
                val lpkClass = XposedHelpers.findClassIfExists(
                    "com.google.android.apps.inputmethod.latin.keyboard.LatinPrimeKeyboard",
                    classLoader
                )
                if (lpkClass != null) {
                    for (field in lpkClass.declaredFields) {
                        if (Modifier.isStatic(field.modifiers)) {
                            field.isAccessible = true
                            val obj = field.get(null) ?: continue
                            val objClass = obj.javaClass
                            try {
                                objClass.getDeclaredMethod("g")
                                val fA = objClass.getDeclaredField("a")
                                if (fA.type == String::class.java) {
                                    hookFlagClass(objClass)
                                    hooked = true
                                    XposedBridge.log("[$TAG] Successfully hooked dynamically detected flag class: ${objClass.name}")
                                    break
                                }
                            } catch (_: Throwable) {}
                        }
                    }
                }
            } catch (t: Throwable) {
                XposedBridge.log("[$TAG] Dynamic flag class detection error: ${t.message}")
            }
        }

        if (!hooked) {
            XposedBridge.log("[$TAG] Warning: No known flag class found in this Gboard version")
        }
    }

    private fun hookFlagClass(clazz: Class<*>) {
        XposedHelpers.findAndHookMethod(clazz, "g", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                handleFlagGetter(param)
            }
        })
    }

    private fun handleFlagGetter(param: XC_MethodHook.MethodHookParam) {
        if (!getBool("pref_master_switch", true)) {
            return
        }

        val receiver = param.thisObject ?: return
        val flagName = resolveFlagName(receiver) ?: return
        val original = param.result

        // 1. Fast word learning count
        if (flagName == "intended_oov_min_count" && getBool("pref_word_learning", true)) {
            param.result = 1L
            logOverride(flagName, 1L)
            return
        }

        // 2. Activation type for Rambler Agentic Dictation
        if (flagName == "ad_activation_type" && getBool("pref_rambler_voice", true)) {
            param.result = 2L
            logOverride(flagName, 2L)
            return
        }

        // 3. AI Writing Tools String flags (models & languages)
        if (getBool("pref_writing_tools", true)) {
            if (getBool("pref_all_keyboards", true)) {
                if (flagName == "writing_helper_supported_language_tags" ||
                    flagName == "llm_pc_supported_language_tags" ||
                    flagName == "proofread_supported_apps"
                ) {
                    param.result = "*"
                    logOverride(flagName, "*")
                    return
                }
            }

            if (flagName == "writing_helper_model_version") {
                param.result = "202408051448_prod_sd_config"
                logOverride(flagName, param.result)
                return
            }
            if (flagName == "writing_helper_text_stylization_model_version") {
                param.result = "202504090000_writing_tools_config"
                logOverride(flagName, param.result)
                return
            }
            if (flagName == "nga_lab_modeless_smartedit_regex_version") {
                param.result = "v3"
                logOverride(flagName, param.result)
                return
            }

            // Backend Type: Force Cloud ASTREA (3L) by default to bypass on-device AICore/Nano
            if (flagName == "writing_tools_v2_backend_type") {
                val backendPref = getString("pref_backend_type", "PRIVATE_INFERENCE_ASTREA")
                val backendType = when (backendPref) {
                    "PRIVATE_INFERENCE_AICORE" -> 2L
                    "GBOARD_SERVER" -> 1L
                    else -> 3L // PRIVATE_INFERENCE_ASTREA (Cloud)
                }
                param.result = backendType
                XposedBridge.log("[$TAG] Override: writing_tools_v2_backend_type -> $backendType ($backendPref)")
                return
            }

            val backendPref = getString("pref_backend_type", "PRIVATE_INFERENCE_ASTREA")
            if (backendPref != "PRIVATE_INFERENCE_AICORE") {
                if (flagName == "writing_tools_enable_hybrid") {
                    param.result = false
                    XposedBridge.log("[$TAG] Override: writing_tools_enable_hybrid -> false")
                    return
                }
                if (flagName == "enable_on_device_proofread") {
                    param.result = false
                    XposedBridge.log("[$TAG] Override: enable_on_device_proofread -> false")
                    return
                }
            }
        }

        // 4. Boolean Flags
        if (original is Boolean) {
            // Voice typing
            if (getBool("pref_rambler_voice", true) && RAMBLER_VOICE_BOOLEAN_FLAGS.contains(flagName)) {
                param.result = true
                logOverride(flagName, true)
                return
            }

            // AI Writing Tools Core
            if (getBool("pref_writing_tools", true) && AI_WRITING_TOOLS_CORE_FLAGS.contains(flagName)) {
                param.result = true
                logOverride(flagName, true)
                return
            }

            // AI Writing Tools V2
            if (getBool("pref_writing_tools", true) &&
                getBool("pref_writing_tools_v2", true) &&
                AI_WRITING_TOOLS_V2_FLAGS.contains(flagName)
            ) {
                param.result = true
                logOverride(flagName, true)
                return
            }

            val uiTweaksEnabled = getBool("pref_enable_ui_tweaks", false)

            // Non-functional UI styling flags (only if UI tweaks master switch is ON)
            if (uiTweaksEnabled) {
                if (getBool("pref_rounded_keys", true) && ROUNDED_KEYS_FLAGS.contains(flagName)) {
                    param.result = true
                    logOverride(flagName, true)
                    return
                }

                if (UI_STYLING_FLAGS.contains(flagName)) {
                    param.result = true
                    logOverride(flagName, true)
                    return
                }
            }

            // Functional Typing & Word learning & Intelligence
            if (getBool("pref_word_learning", true) && TYPING_AND_EXPRESSION_FLAGS.contains(flagName)) {
                param.result = true
                logOverride(flagName, true)
                return
            }
        }
    }

    private fun resolveFlagName(receiver: Any): String? {
        val clazz = receiver.javaClass
        var field = flagNameFieldCache[clazz]
        if (field == null) {
            try {
                // In Phenotype flag classes (adbx, ajoh, nxw, etc.), field 'a' is the String name
                val f = clazz.getDeclaredField("a")
                if (f.type == String::class.java) {
                    f.isAccessible = true
                    flagNameFieldCache[clazz] = f
                    field = f
                }
            } catch (t: Throwable) {
                for (f in clazz.declaredFields) {
                    if (f.type == String::class.java) {
                        f.isAccessible = true
                        flagNameFieldCache[clazz] = f
                        field = f
                        break
                    }
                }
            }
        }
        return try {
            field?.get(receiver) as? String
        } catch (t: Throwable) {
            null
        }
    }
}
