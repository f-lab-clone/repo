
function uuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
}

const getPrefix = () => {
    return `K6-${uuid().substring(0, 10)}`;
}

const User = () => ({
    name: `${getPrefix()}-name`,
    email: `${getPrefix()}@email.com`,
    password: `${getPrefix()}-password`,
});

export default {
    User
}