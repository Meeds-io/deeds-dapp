<script>
import routes from './../routes';

export default {
  data: () => ({
    currentRoute: window.location.pathname || '/',
  }),
  computed: Vuex.mapState({
    viewComponent(state) {
      if (state.address) {
        const pathParts = this.currentRoute.split('/');
        const route = pathParts[pathParts.length - 1];
        return routes[`/${route}`] || {template: '<p>Not Found</p>'};
      } else {
        return {template: '<deeds-wallet-connect />'};
      }
    },
    address: state => state.address,
  }),
  created() {
    this.$root.$on('location-change', () => this.currentRoute = window.location.pathname || '/');
  },
  render(h) {
    return h(this.viewComponent);
  },
};
</script>