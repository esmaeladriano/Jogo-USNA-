extends Node

# Autoload para persistência local em user://usna_save.json
const SAVE_PATH = "user://usna_save.json"

var unlocked_levels: Array = [1]
var level_stars: Dictionary = {} # level_id (int) -> stars (int 1..3)
var level_times: Dictionary = {} # level_id (int) -> time (float)

# Definições
var sound_volume: float = 0.8
var music_volume: float = 0.5
var vibration_enabled: bool = true
var language: String = "pt"

func _ready() -> void:
	load_game()

func save_game() -> void:
	var data = {
		"unlocked_levels": unlocked_levels,
		"level_stars": level_stars,
		"level_times": level_times,
		"sound_volume": sound_volume,
		"music_volume": music_volume,
		"vibration_enabled": vibration_enabled,
		"language": language
	}
	var file = FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		file.store_string(JSON.stringify(data, "\t"))
		file.close()

func load_game() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		return
	var file = FileAccess.open(SAVE_PATH, FileAccess.READ)
	if file:
		var content = file.get_as_text()
		file.close()
		var json = JSON.new()
		var error = json.parse(content)
		if error == OK and typeof(json.data) == TYPE_DICTIONARY:
			var dict = json.data
			unlocked_levels = dict.get("unlocked_levels", [1])
			level_stars = dict.get("level_stars", {})
			level_times = dict.get("level_times", {})
			sound_volume = dict.get("sound_volume", 0.8)
			music_volume = dict.get("music_volume", 0.5)
			vibration_enabled = dict.get("vibration_enabled", true)
			language = dict.get("language", "pt")

func complete_level(level_id: int, stars: int, time_sec: float) -> void:
	var prev_stars = int(level_stars.get(str(level_id), 0))
	level_stars[str(level_id)] = max(prev_stars, stars)

	var prev_time = float(level_times.get(str(level_id), 9999.0))
	level_times[str(level_id)] = min(prev_time, time_sec)

	var next_lvl = level_id + 1
	if next_lvl <= 10 and not unlocked_levels.has(next_lvl):
		unlocked_levels.append(next_lvl)

	save_game()
