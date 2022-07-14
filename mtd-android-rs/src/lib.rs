#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jlong};

use mtd::TdList;

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_newTdList(_: JNIEnv, _: JClass) -> jlong {
    Box::into_raw(Box::new(TdList::new_client())) as jlong
}

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_newTdListFromJson(env: JNIEnv, _: JClass, json: JString) -> jlong {
    let json = env.get_string(json);

    if json.is_ok() {
        let json: String = json.unwrap().into();

        if let Ok(tdlist) = TdList::new_from_json(&json) {
            return Box::into_raw(Box::new(tdlist)) as jlong;
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