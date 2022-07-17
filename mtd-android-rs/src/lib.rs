#![allow(non_snake_case)]

use std::net::SocketAddr;
use chrono::Weekday;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jboolean, jbyte, jbyteArray, jint, jlong, jlongArray, jshort, jsize, jstring};

use mtd::{Config, MtdNetMgr, Task, TdList, Todo, weekday_to_date};

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_newTdList(_: JNIEnv, _: JClass) -> jlong {
    Box::into_raw(Box::new(TdList::new_client())) as jlong
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_newTdListFromJson(env: JNIEnv, _: JClass, json: JString) -> jlong {
    let json = env.get_string(json);

    if let Ok(json) = json {
        let json: String = json.into();

        if let Ok(td_list) = TdList::new_from_json(&json) {
            return Box::into_raw(Box::new(td_list)) as jlong;
        }
    }

    env.throw_new("java/lang/IllegalArgumentException", "Invalid json string.").unwrap();

    return 0 as jlong;
}

#[no_mangle]
pub unsafe extern "system" fn Java_com_github_windore_mtd_Mtd_destroyTdList(_: JNIEnv, _: JClass, td_list_ptr: jlong) {
    // Take ownership to free memory
    let _ = Box::from_raw(td_list_ptr as *mut TdList);
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_toJson(env: JNIEnv, _: JClass, td_list_ptr: jlong) -> jstring {
    let td_list = unsafe { &*(td_list_ptr as *mut TdList) };

    if let Ok(json) = td_list.to_json() {
        if let Ok(string) = env.new_string(json) {
            return string.into_inner();
        }
    }

    JObject::null().into_inner()
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_getItemsForWeekday(
    env: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    weekday_num: jbyte,
    item_type_num: jshort,
    are_done: jboolean
) -> jlongArray {
    let td_list = unsafe { &*(td_list_ptr as *mut TdList) };
    let weekday = byte_to_weekday(weekday_num as u8);
    let item_type = ItemType::from(item_type_num);
    let are_done = are_done != 0;

    let date = weekday_to_date(weekday);
    let mut id_list = Vec::new();

    match item_type {
        ItemType::Todo => {
            let todos;
            if are_done {
                todos = td_list.done_todos_for_date(date);
            } else {
                todos = td_list.undone_todos_for_date(date);
            }
            for todo in todos {
                id_list.push(todo.id() as jlong);
            }
        }
        ItemType::Task => {
            let tasks;
            if are_done {
                tasks = td_list.done_tasks_for_date(date);
            } else {
                tasks = td_list.undone_tasks_for_date(date);
            }
            for task in tasks {
                id_list.push(task.id() as jlong);
            }
        }
    }

    if let Ok(arr) = env.new_long_array(id_list.len() as jsize) {
        env.set_long_array_region(arr, 0, &id_list).unwrap();
        arr
    } else {
        // In case of errors return null, this is better because then the error will show up properly
        // in Java side
        JObject::null().into_inner()
    }

}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_getItemBody(
    env: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    item_type_num: jshort,
    item_id: jlong
) -> jstring {
    // Borrow as mut bcs there is no get_todo/task without mut
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };
    let item_type = ItemType::from(item_type_num);
    let id = item_id as u64;

    let mut body_opt = None;

    match item_type {
        ItemType::Todo => {
            if let Ok(todo) = td_list.get_todo_mut(id) {
                body_opt = Some(todo.body());
            }
        }
        ItemType::Task => {
            if let Ok(task) = td_list.get_task_mut(id) {
                body_opt = Some(task.body());
            }
        }
    }

    if let Some(body) = body_opt {
        if let Ok(s) = env.new_string(body) {
            return s.into_inner();
        }
    }

    JObject::null().into_inner()
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_addTodo(
    env: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    body: JString,
    weekday_num: jbyte
) {
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };
    let weekday = byte_to_weekday(weekday_num as u8);

    if let Ok(body) = env.get_string(body) {
        td_list.add_todo(Todo::new_dated(body.into(), weekday));
    } else {
        env.throw_new("java/lang/IllegalArgumentException", "Invalid body string.").unwrap();
    }
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_addTask(
    env: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    body: JString,
    weekday_nums: jbyteArray
) {
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };

    let body = env.get_string(body);
    let weekday_nums = env.convert_byte_array(weekday_nums);

    if let Err(e) = body {
        env.throw_new("java/lang/IllegalArgumentException", format!("Invalid body string: {:?}.", e)).unwrap();
        return;
    }

    if let Err(e) = weekday_nums {
        env.throw_new("java/lang/IllegalArgumentException", format!("Invalid weekday numbers: {:?}.", e)).unwrap();
        return;
    }

    let mut weekdays = Vec::new();

    for wd in weekday_nums.unwrap() {
        weekdays.push(byte_to_weekday(wd));
    }

    if weekdays.is_empty() {
        env.throw_new("java/lang/IllegalArgumentException", "Empty weekday array.").unwrap();
        return;
    }

    td_list.add_task(Task::new(body.unwrap().into(), weekdays));
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_removeItem(
    _: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    item_type_num: jshort,
    item_id: jlong
) -> jint {
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };
    let id = item_id as u64;

    let result;

    match ItemType::from(item_type_num) {
        ItemType::Todo => {
            result = td_list.remove_todo(id);
        }
        ItemType::Task => {
            result = td_list.remove_task(id);
        }
    }

    if result.is_ok() {
        0
    } else {
        1
    }
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_modifyItemDoneState(
    _: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    item_type_num: jshort,
    item_id: jlong,
    done: jboolean,
    done_weekday_num: jbyte
) -> jint {
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };
    let id = item_id as u64;
    let done = done != 0;
    let weekday = byte_to_weekday(done_weekday_num as u8);

    let mut result = Err(());

    match ItemType::from(item_type_num) {
        ItemType::Todo => {
            if let Ok(todo) = td_list.get_todo_mut(id) {
                todo.set_done(done);
                result = Ok(());
            }
        }
        ItemType::Task => {
            if let Ok(task) = td_list.get_task_mut(id) {
                task.set_done(done, weekday_to_date(weekday));
                result = Ok(());
            }
        }
    }

    if result.is_ok() {
        0
    } else {
        1
    }
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_sync(
    env: JNIEnv,
    _: JClass,
    td_list_ptr: jlong,
    password: jbyteArray,
    socket_addr: JString
) -> jstring {
    let td_list = unsafe { &mut *(td_list_ptr as *mut TdList) };

    if let Ok(js) = env.new_string(sync(env, td_list, password, socket_addr)) {
        js.into_inner()
    } else {
        JObject::null().into_inner()
    }
}

// Sync returns an empty string on success and a string containing an error message on failure.
fn sync(env: JNIEnv, list: &mut TdList, password: jbyteArray, socket_addr: JString) -> String {
    let password = env.convert_byte_array(password);
    let socket_addr = env.get_string(socket_addr);

    if let Err(e) = password {
        env.throw_new("java/lang/IllegalArgumentException", format!("Invalid password byte array: {:?}.", e)).unwrap();
        return "Error".to_string();
    }

    if let Err(e) = socket_addr {
        env.throw_new("java/lang/IllegalArgumentException", format!("Invalid socket address string: {:?}.", e)).unwrap();
        return "Error".to_string();
    }

    let password = password.unwrap();
    let socket_addr: String = socket_addr.unwrap().into();

    return if let Ok(valid_socket_addr) = socket_addr.parse::<SocketAddr>() {
        let conf = Config::new_default(password, valid_socket_addr, None);

        let mut sync_mgr = MtdNetMgr::new(list, &conf);

        if let Err(e) = sync_mgr.client_sync() {
            format!("An error occurred while syncing: {}", e)
        } else {
            "".to_string()
        }
    } else {
        format!("Cannot parse '{}' to a socket address.", socket_addr)
    }
}

enum ItemType {
    Todo,
    Task
}

impl From<jshort> for ItemType {
    fn from(n: jshort) -> Self {
        match n {
            1 => ItemType::Todo,
            _ => ItemType::Task,
        }
    }
}

fn byte_to_weekday(wd_num: u8) -> Weekday {
    match wd_num {
        1 => Weekday::Mon,
        2 => Weekday::Tue,
        3 => Weekday::Wed,
        4 => Weekday::Thu,
        5 => Weekday::Fri,
        6 => Weekday::Sat,
        _ => Weekday::Sun,
    }
}
