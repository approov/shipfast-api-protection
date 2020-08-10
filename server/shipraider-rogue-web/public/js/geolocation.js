const geoFindMe = function(e) {

  e.preventDefault()

  const status = document.querySelector('#status');

  function success(position) {
    document.querySelector('#location-latitude-input').value = position.coords.latitude
    document.querySelector('#location-longitude-input').value = position.coords.longitude
  }

  function error(error) {
    console.debug(error)
    alert("GEOLOCATION ERROR\n\n" + error.message + "\n\nSome browsers are not able to retrieve the coordinates.\n\nPlease try Chrome instead.")
  }

  if(navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(success, error)
  } else {
    alert('Geolocation is not supported by your browser')
  }
}

document.querySelector('#find-me').addEventListener('click', geoFindMe);
