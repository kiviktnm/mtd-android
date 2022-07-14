use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_com_github_windore_mtd_Mtd_hello(env: JNIEnv, _: JClass, input: JString) -> jstring {
    let inp: String = env.get_string(input).expect("Couldn't get java string.").into();
    let output = env.new_string(format!("Hello, {}", inp)).expect("Couldn't create java string!");
    output.into_inner()
}