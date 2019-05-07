import os
from sys import argv

not_xxhdpi_array = ['-xxxhdpi', '-xhdpi', '-hdpi', '-mdpi']
not_xhdpi_array = ['-xxxhdpi', '-hdpi', '-mdpi']

ignore_list = [
    'danger_scan_button',
    'result_page_app_lock_fullscreen',
    'risk_scan_button',
    'safe_scan_button',
    'scan_button_click_mask',
    'scan_button_mask',
    'scan_result_virus',
    'scanning_bg',
    'scanning_rotate_light',
    'security_main_hola'
]


def is_not_xxhdpi_folder(path):
    for dpi in not_xxhdpi_array:
        if path.find(dpi) >= 0:
            return True
    return False


def is_not_xhdpi_folder(path):
    for dpi in not_xhdpi_array:
        if path.find(dpi) >= 0:
            return True
    return False


def get_xxhdpi_folder(path):
    for dpi in not_xxhdpi_array:
        if path.find(dpi) >= 0:
            return path.replace(dpi, '-xxhdpi')
    return ''


def get_xhdpi_folder(path):
    for dpi in not_xxhdpi_array:
        if path.find(dpi) >= 0:
            return path.replace(dpi, '-xhdpi')
    return ''


def should_ignore(file_name):
    for ignore_item in ignore_list:
        if file_name.find(ignore_item) >= 0:
            return True
    return False


def remove_duplicated_files(folder, reference_folder):
    print folder, reference_folder
    for file_name in os.listdir(folder):
        if os.path.isfile(folder + '/' + file_name):
            if os.path.exists(reference_folder + '/' + file_name):
                if not should_ignore(file_name):
                    os.remove(folder + '/' + file_name)
                    print 'remove file ' + folder + '/' + file_name


if __name__ == '__main__':
    path = argv[1] + '/'
    print path
    drawable_path_array = []

    for directory in os.listdir(path):
        if os.path.isdir(path + directory):
            if directory.find('drawable') >= 0:
                drawable_path_array.append(path + directory)

    for drawable_path in drawable_path_array:
        if is_not_xxhdpi_folder(drawable_path):
            remove_duplicated_files(drawable_path, get_xxhdpi_folder(drawable_path))

    for drawable_path in drawable_path_array:
        if is_not_xhdpi_folder(drawable_path):
            remove_duplicated_files(drawable_path, get_xhdpi_folder(drawable_path))
