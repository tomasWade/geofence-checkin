import { useAuthStore } from '../stores/auth'

function check(el, binding) {
  const auth = useAuthStore()
  if (binding.value && !auth.hasPerm(binding.value)) {
    el.parentNode && el.parentNode.removeChild(el)
  }
}

export default {
  mounted: check,
  updated: check,
}
